package io.github.ragHub.retrieval.pipeline;

import io.github.ragHub.core.domain.ChatMessage;
import io.github.ragHub.core.domain.RagAnswer;
import io.github.ragHub.core.domain.StreamChunk;
import io.github.ragHub.core.port.RagQueryPort;
import io.github.ragHub.retrieval.reranker.KeywordReranker;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagPipeline implements RagQueryPort {

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant. Answer questions based ONLY on the provided context.
            If the context does not contain enough information, say so clearly.
            Always cite your sources.
            """;

    @Value("${rag.provider:openai}")
    private String providerName;

    private final ChatClient chatClient;
    private final KeywordReranker reranker;

    public RagPipeline(ChatClient.Builder builder, VectorStore vectorStore, KeywordReranker reranker) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
        this.reranker = reranker;
    }

    private List<org.springframework.ai.chat.messages.Message> toSpringMessages(ChatMessage.ConversationContext ctx) {
        if (ctx == null || ctx.history() == null || ctx.history().isEmpty()) return List.of();
        return ctx.history().stream()
                .<org.springframework.ai.chat.messages.Message>map(m -> "user".equals(m.role())
                        ? new UserMessage(m.content())
                        : new AssistantMessage(m.content()))
                .toList();
    }

    @Override
    public RagAnswer query(String question, ChatMessage.ConversationContext context) {
        long start = System.currentTimeMillis();
        var response = chatClient.prompt()
                .messages(toSpringMessages(context))
                .user(question)
                .call()
                .chatResponse();
        String answer = response.getResult().getOutput().getText();
        return new RagAnswer(answer, extractSources(response, question), providerName, System.currentTimeMillis() - start);
    }

    @SuppressWarnings("unchecked")
    private List<RagAnswer.SourceReference> extractSources(ChatResponse response, String question) {
        Object raw = response.getMetadata().get("retrieved_documents");
        if (!(raw instanceof List<?> docs) || docs.isEmpty()) return List.of();
        List<org.springframework.ai.document.Document> documents = docs.stream()
                .filter(d -> d instanceof org.springframework.ai.document.Document)
                .map(d -> (org.springframework.ai.document.Document) d)
                .collect(Collectors.toList());
        List<org.springframework.ai.document.Document> reranked = reranker.rerank(question, documents);
        return reranked.stream()
                .map(d -> new RagAnswer.SourceReference(
                        (String) d.getMetadata().getOrDefault("documentId", d.getId()),
                        (String) d.getMetadata().getOrDefault("title", ""),
                        d.getText() != null && d.getText().length() > 200 ? d.getText().substring(0, 200) : d.getText(),
                        0.0))
                .toList();
    }

    @Override
    public Flux<StreamChunk> queryStream(String question, ChatMessage.ConversationContext context) {
        long start = System.currentTimeMillis();
        Flux<StreamChunk> tokens = chatClient.prompt()
                .messages(toSpringMessages(context))
                .user(question)
                .stream()
                .content()
                .map(StreamChunk.Token::new);

        StreamChunk.Done done = new StreamChunk.Done(List.of(), providerName, System.currentTimeMillis() - start);
        return Flux.concat(tokens, Flux.just(done));
    }
}
