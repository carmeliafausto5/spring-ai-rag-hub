package io.github.ragHub.core.domain;

import java.util.List;

public record ChatMessage(String role, String content) {

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }

    public record ConversationContext(String sessionId, List<ChatMessage> history) {}
}
