package io.github.ragHub.core.port;

import io.github.ragHub.core.domain.KnowledgeDocument;
import reactor.core.publisher.Flux;

public interface DocumentIngestionPort {
    void ingest(KnowledgeDocument document);
    void ingestBatch(Flux<KnowledgeDocument> documents);
    void delete(String documentId);
}
