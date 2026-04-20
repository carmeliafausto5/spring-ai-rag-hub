package io.github.ragHub.core.port;

import java.util.List;

public interface DocumentQueryPort {
    record DocumentSummary(String id, String title, String sourceUri) {}
    List<DocumentSummary> listDocuments();
}
