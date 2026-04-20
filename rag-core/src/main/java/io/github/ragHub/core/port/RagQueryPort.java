package io.github.ragHub.core.port;

import io.github.ragHub.core.domain.ChatMessage;
import io.github.ragHub.core.domain.RagAnswer;
import io.github.ragHub.core.domain.StreamChunk;
import reactor.core.publisher.Flux;

public interface RagQueryPort {
    RagAnswer query(String question, ChatMessage.ConversationContext context);
    Flux<StreamChunk> queryStream(String question, ChatMessage.ConversationContext context);
}
