package io.github.ragHub.api.service;

import io.github.ragHub.api.entity.Conversation;
import io.github.ragHub.api.entity.ConversationMessage;
import io.github.ragHub.api.repository.ConversationRepository;
import io.github.ragHub.core.domain.ChatMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository repo;

    public ConversationService(ConversationRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public List<ChatMessage> loadHistory(String sessionId) {
        return repo.findById(sessionId)
                .map(c -> c.getMessages().stream()
                        .map(m -> new ChatMessage(m.getRole(), m.getContent()))
                        .toList())
                .orElse(List.of());
    }

    @Transactional
    public void appendMessages(String sessionId, String userQuestion, String assistantAnswer) {
        Conversation conv = repo.findById(sessionId).orElseGet(() -> {
            Conversation c = new Conversation(sessionId);
            return repo.save(c);
        });
        conv.getMessages().add(new ConversationMessage(conv, "user", userQuestion));
        conv.getMessages().add(new ConversationMessage(conv, "assistant", assistantAnswer));
        conv.setUpdatedAt(LocalDateTime.now());
        repo.save(conv);
    }

    @Transactional(readOnly = true)
    public List<Conversation> listConversations() {
        return repo.findAll();
    }

    @Transactional
    public void deleteConversation(String sessionId) {
        repo.deleteById(sessionId);
    }
}
