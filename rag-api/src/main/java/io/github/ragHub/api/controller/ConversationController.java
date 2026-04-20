package io.github.ragHub.api.controller;

import io.github.ragHub.api.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService service;

    public ConversationController(ConversationService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(service.listConversations().stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getId(),
                        "createdAt", c.getCreatedAt().toString(),
                        "updatedAt", c.getUpdatedAt().toString(),
                        "messageCount", c.getMessages().size()
                ))
                .toList());
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Map<String, String>>> messages(@PathVariable String id) {
        return ResponseEntity.ok(service.loadHistory(id).stream()
                .map(m -> Map.of("role", m.role(), "content", m.content()))
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
}
