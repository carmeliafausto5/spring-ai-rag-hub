package io.github.ragHub.ingestion.service;

import io.github.ragHub.core.domain.KnowledgeDocument;
import io.github.ragHub.core.port.DocumentIngestionPort;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class IngestionService implements DocumentIngestionPort {

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.splitter = new TokenTextSplitter(512, 64, 5, 10000, true);
    }

    @Override
    public void ingest(KnowledgeDocument doc) {
        List<Document> chunks = splitter.apply(List.of(toSpringDoc(doc)));
        vectorStore.add(chunks);
    }

    @Override
    public void ingestBatch(Flux<KnowledgeDocument> documents) {
        documents.collectList()
                .map(docs -> docs.stream().map(this::toSpringDoc).toList())
                .map(splitter::apply)
                .subscribe(vectorStore::add);
    }

    @Override
    public void delete(String documentId) {
        vectorStore.delete(List.of(documentId));
    }

    private Document toSpringDoc(KnowledgeDocument doc) {
        Map<String, Object> meta = new java.util.HashMap<>(doc.metadata());
        meta.put("documentId", doc.id());
        meta.put("title", doc.title());
        meta.put("sourceUri", doc.sourceUri());
        return new Document(doc.id(), doc.content(), meta);
    }
}
