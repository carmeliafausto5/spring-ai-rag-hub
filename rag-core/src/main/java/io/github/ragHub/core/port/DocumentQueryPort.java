package io.github.ragHub.core.port;

import java.util.List;

public interface DocumentQueryPort {
    record DocumentSummary(String id, String title, String sourceUri, List<String> tags) {}
    List<DocumentSummary> listDocuments();
    List<String> listChunkPreviews(String documentId, int limit);
    void updateTags(String documentId, List<String> tags);
}
