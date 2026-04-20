package io.github.ragHub.retrieval.pipeline;

import io.github.ragHub.core.domain.ChatMessage;
import io.github.ragHub.core.domain.RagAnswer;
import io.github.ragHub.core.domain.SearchMode;
import io.github.ragHub.core.domain.StreamChunk;
import io.github.ragHub.core.port.ProviderSettingsPort;
import io.github.ragHub.core.port.RagQueryPort;
import io.github.ragHub.retrieval.retriever.BM25Retriever;
import io.github.ragHub.retrieval.reranker.KeywordReranker;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RagPipeline implements RagQueryPort {

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant. Answer questions based ONLY on the provided context.
            If the context does not contain enough information, say so clearly.
            Always cite your sources.
            """;

    private static final int TOP_K = 5;

    private final ChatClient advisorClient;
    private final ChatClient plainClient;
    private final VectorStore vectorStore;
    private final BM25Retriever bm25Retriever;
    private final KeywordReranker reranker;
    private final ProviderSettingsPort settings;

    public RagPipeline(ChatClient.Builder builder, VectorStore vectorStore,
                       BM25Retriever bm25Retriever, KeywordReranker reranker,
                       ProviderSettingsPort settings) {
        this.vectorStore = vectorStore;
        this.bm25Retriever = bm25Retriever;
        this.reranker = reranker;
        this.settings = settings;
        this.advisorClient = builder.defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
        this.plainClient = builder.defaultSystem(SYSTEM_PROMPT).build();
    }

    @Override
    public RagAnswer query(String question, ChatMessage.ConversationContext context, SearchMode mode) {
        long start = System.currentTimeMillis();
        if (mode == SearchMode.VECTOR) {
            var response = advisorClient.prompt()
                    .messages(toSpringMessages(context)).user(question)
                    .call().chatResponse();
            return new RagAnswer(response.getResult().getOutput().getText(),
                    extractSources(response, question), settings.get("rag.provider"),
                    System.currentTimeMillis() - start);
        }
        List<Document> docs = retrieveDocs(question, mode);
        String answer = plainClient.prompt()
                .messages(toSpringMessages(context))
                .user(buildContextPrompt(question, docs))
                .call().content();
        List<RagAnswer.SourceReference> sources = toSources(reranker.rerank(question, docs));
        return new RagAnswer(answer, sources, settings.get("rag.provider"), System.currentTimeMillis() - start);
    }

    @Override
    public Flux<StreamChunk> queryStream(String question, ChatMessage.ConversationContext context, SearchMode mode) {
        long start = System.currentTimeMillis();
        ChatClient client = mode == SearchMode.VECTOR ? advisorClient : plainClient;
        String userMsg = mode == SearchMode.VECTOR ? question
                : buildContextPrompt(question, retrieveDocs(question, mode));
        Flux<StreamChunk> tokens = client.prompt()
                .messages(toSpringMessages(context)).user(userMsg)
                .stream().content().map(StreamChunk.Token::new);
        return Flux.concat(tokens, Flux.just(
                new StreamChunk.Done(List.of(), settings.get("rag.provider"), System.currentTimeMillis() - start)));
    }

    private List<Document> retrieveDocs(String question, SearchMode mode) {
        List<Document> vectorDocs = mode != SearchMode.BM25
                ? vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(TOP_K).build())
                : List.of();
        List<Document> bm25Docs = mode != SearchMode.VECTOR
                ? bm25Retriever.retrieve(question, TOP_K)
                : List.of();
        return mode == SearchMode.HYBRID ? rrfFuse(vectorDocs, bm25Docs) :
               mode == SearchMode.BM25 ? bm25Docs : vectorDocs;
    }

    private List<Document> rrfFuse(List<Document> vectorDocs, List<Document> bm25Docs) {
        Map<String, Double> scores = new LinkedHashMap<>();
        for (int i = 0; i < vectorDocs.size(); i++)
            scores.merge(vectorDocs.get(i).getId(), 1.0 / (60 + i + 1), Double::sum);
        for (int i = 0; i < bm25Docs.size(); i++)
            scores.merge(bm25Docs.get(i).getId(), 1.0 / (60 + i + 1), Double::sum);
        Map<String, Document> byId = Stream.concat(vectorDocs.stream(), bm25Docs.stream())
                .collect(Collectors.toMap(Document::getId, d -> d, (a, b) -> a));
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> byId.get(e.getKey())).filter(Objects::nonNull)
                .limit(TOP_K).toList();
    }

    private String buildContextPrompt(String question, List<Document> docs) {
        StringBuilder sb = new StringBuilder("Context:\n");
        docs.forEach(d -> sb.append(d.getText()).append("\n\n"));
        return sb.append("Question: ").append(question).toString();
    }

    private List<org.springframework.ai.chat.messages.Message> toSpringMessages(ChatMessage.ConversationContext ctx) {
        if (ctx == null || ctx.history() == null || ctx.history().isEmpty()) return List.of();
        return ctx.history().stream()
                .<org.springframework.ai.chat.messages.Message>map(m -> "user".equals(m.role())
                        ? new UserMessage(m.content()) : new AssistantMessage(m.content()))
                .toList();
    }

    private List<RagAnswer.SourceReference> extractSources(ChatResponse response, String question) {
        Object raw = response.getMetadata().get("retrieved_documents");
        if (!(raw instanceof List<?> docs) || docs.isEmpty()) return List.of();
        List<Document> documents = docs.stream()
                .filter(d -> d instanceof Document).map(d -> (Document) d)
                .collect(Collectors.toList());
        return toSources(reranker.rerank(question, documents));
    }

    private List<RagAnswer.SourceReference> toSources(List<Document> docs) {
        return docs.stream().map(d -> new RagAnswer.SourceReference(
                (String) d.getMetadata().getOrDefault("documentId", d.getId()),
                (String) d.getMetadata().getOrDefault("title", ""),
                d.getText() != null && d.getText().length() > 200 ? d.getText().substring(0, 200) : d.getText(),
                0.0)).toList();
    }
}
