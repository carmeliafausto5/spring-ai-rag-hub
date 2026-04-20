package io.github.ragHub.core.port;

import io.github.ragHub.core.domain.ChatMessage;
import io.github.ragHub.core.domain.RagAnswer;
import io.github.ragHub.core.domain.SearchMode;
import io.github.ragHub.core.domain.StreamChunk;
import reactor.core.publisher.Flux;

public interface RagQueryPort {
    RagAnswer query(String question, ChatMessage.ConversationContext context, SearchMode mode);
    Flux<StreamChunk> queryStream(String question, ChatMessage.ConversationContext context, SearchMode mode);

    default RagAnswer query(String question, ChatMessage.ConversationContext context) {
        return query(question, context, SearchMode.VECTOR);
    }
    default Flux<StreamChunk> queryStream(String question, ChatMessage.ConversationContext context) {
        return queryStream(question, context, SearchMode.VECTOR);
    }
}
