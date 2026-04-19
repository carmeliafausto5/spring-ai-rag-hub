package io.github.ragHub.core.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record KnowledgeDocument(
        String id,
        String title,
        String content,
        String sourceUri,
        Map<String, Object> metadata,
        Instant createdAt
) {
    public static KnowledgeDocument of(String title, String content, String sourceUri, Map<String, Object> metadata) {
        return new KnowledgeDocument(
                UUID.randomUUID().toString(), title, content, sourceUri, metadata, Instant.now()
        );
    }
}
