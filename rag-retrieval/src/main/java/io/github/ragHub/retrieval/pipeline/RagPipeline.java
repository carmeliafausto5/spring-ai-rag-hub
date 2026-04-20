package io.github.ragHub.retrieval.pipeline;

import io.github.ragHub.core.domain.ChatMessage;
import io.github.ragHub.core.domain.RagAnswer;
import io.github.ragHub.core.domain.StreamChunk;
import io.github.ragHub.core.port.RagQueryPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${rag.provider:openai}")
    private String providerName;

    private final ChatClient chatClient;

    public RagPipeline(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
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
        String answer = chatClient.prompt()
                .messages(toSpringMessages(context))
                .user(question)
                .call()
                .content();
        return new RagAnswer(answer, List.of(), providerName, System.currentTimeMillis() - start);
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
