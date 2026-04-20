package io.github.ragHub.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ragHub.api.dto.QueryRequest;
import io.github.ragHub.api.service.ConversationService;
import io.github.ragHub.core.domain.ChatMessage;
import io.github.ragHub.core.domain.RagAnswer;
import io.github.ragHub.core.domain.SearchMode;
import io.github.ragHub.core.domain.StreamChunk;
import io.github.ragHub.core.port.RagQueryPort;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    private final RagQueryPort ragQueryPort;
    private final ObjectMapper objectMapper;
    private final ConversationService conversationService;

    public RagController(RagQueryPort ragQueryPort, ObjectMapper objectMapper, ConversationService conversationService) {
        this.ragQueryPort = ragQueryPort;
        this.objectMapper = objectMapper;
        this.conversationService = conversationService;
    }

    @PostMapping("/query")
    public RagAnswer query(@Valid @RequestBody QueryRequest request) {
        var mode = request.searchMode() != null ? request.searchMode() : SearchMode.VECTOR;
        var answer = ragQueryPort.query(request.question(), buildContext(request), mode);
        if (request.sessionId() != null && !request.sessionId().isBlank()) {
            conversationService.appendMessages(request.sessionId(), request.question(), answer.answer());
        }
        return answer;
    }

    @PostMapping(value = "/query/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> queryStream(@Valid @RequestBody QueryRequest request) {
        var mode = request.searchMode() != null ? request.searchMode() : SearchMode.VECTOR;
        return ragQueryPort.queryStream(request.question(), buildContext(request), mode)
                .map(chunk -> {
                    if (chunk instanceof StreamChunk.Token t) {
                        return ServerSentEvent.<String>builder().event("token").data(t.text()).build();
                    }
                    StreamChunk.Done d = (StreamChunk.Done) chunk;
                    try {
                        return ServerSentEvent.<String>builder().event("done").data(objectMapper.writeValueAsString(d)).build();
                    } catch (Exception e) {
                        return ServerSentEvent.<String>builder().event("done").data("{}").build();
                    }
                });
    }

    private ChatMessage.ConversationContext buildContext(QueryRequest request) {
        List<ChatMessage> history;
        if (request.sessionId() != null && !request.sessionId().isBlank()) {
            history = conversationService.loadHistory(request.sessionId());
        } else {
            history = request.history() == null ? List.of() :
                    request.history().stream()
                            .map(m -> new ChatMessage(m.role(), m.content()))
                            .toList();
        }
        return new ChatMessage.ConversationContext(request.sessionId(), history);
    }
}
