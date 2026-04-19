package io.github.ragHub.api.controller;

import io.github.ragHub.api.dto.QueryRequest;
import io.github.ragHub.core.domain.ChatMessage;
import io.github.ragHub.core.domain.RagAnswer;
import io.github.ragHub.core.port.RagQueryPort;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RagQueryPort ragQueryPort;

    public RagController(RagQueryPort ragQueryPort) {
        this.ragQueryPort = ragQueryPort;
    }

    @PostMapping("/query")
    public RagAnswer query(@Valid @RequestBody QueryRequest request) {
        return ragQueryPort.query(request.question(), buildContext(request));
    }

    @PostMapping(value = "/query/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> queryStream(@Valid @RequestBody QueryRequest request) {
        return ragQueryPort.queryStream(request.question(), buildContext(request));
    }

    private ChatMessage.ConversationContext buildContext(QueryRequest request) {
        List<ChatMessage> history = request.history() == null ? List.of() :
                request.history().stream()
                        .map(m -> new ChatMessage(m.role(), m.content()))
                        .toList();
        return new ChatMessage.ConversationContext(request.sessionId(), history);
    }
}
