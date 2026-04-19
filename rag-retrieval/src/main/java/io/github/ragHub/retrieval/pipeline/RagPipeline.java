package io.github.ragHub.retrieval.pipeline;

import io.github.ragHub.core.domain.ChatMessage;
import io.github.ragHub.core.domain.RagAnswer;
import io.github.ragHub.core.port.RagQueryPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class RagPipeline implements RagQueryPort {

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant. Answer questions based ONLY on the provided context.
            If the context does not contain enough information, say so clearly.
            Always cite your sources.
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagPipeline(ChatClient.Builder builder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

    @Override
    public RagAnswer query(String question, ChatMessage.ConversationContext context) {
        long start = System.currentTimeMillis();

        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();

        List<RagAnswer.SourceReference> sources = retrieveSources(question);

        return new RagAnswer(answer, sources, "configured-provider", System.currentTimeMillis() - start);
    }

    @Override
    public Flux<String> queryStream(String question, ChatMessage.ConversationContext context) {
        return chatClient.prompt()
                .user(question)
                .stream()
                .content();
    }

    private List<RagAnswer.SourceReference> retrieveSources(String question) {
        return vectorStore.similaritySearch(question).stream()
                .map(doc -> new RagAnswer.SourceReference(
                        (String) doc.getMetadata().getOrDefault("documentId", doc.getId()),
                        (String) doc.getMetadata().getOrDefault("title", "Unknown"),
                        doc.getText().substring(0, Math.min(200, doc.getText().length())),
                        (double) doc.getMetadata().getOrDefault("distance", 0.0)
                ))
                .toList();
    }
}
