package io.github.ragHub.core.port;

import io.github.ragHub.core.domain.ChatMessage;
import io.github.ragHub.core.domain.RagAnswer;
import reactor.core.publisher.Flux;

public interface RagQueryPort {
    RagAnswer query(String question, ChatMessage.ConversationContext context);
    Flux<String> queryStream(String question, ChatMessage.ConversationContext context);
}
