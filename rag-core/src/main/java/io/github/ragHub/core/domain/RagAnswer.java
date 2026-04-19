package io.github.ragHub.core.domain;

import java.util.List;

public record RagAnswer(
        String answer,
        List<SourceReference> sources,
        String provider,
        long latencyMs
) {
    public record SourceReference(String documentId, String title, String excerpt, double score) {}
}
