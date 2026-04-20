package io.github.ragHub.ingestion.service;

import io.github.ragHub.core.domain.KnowledgeDocument;
import io.github.ragHub.core.port.DocumentIngestionPort;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class IngestionService implements DocumentIngestionPort {

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;

    public IngestionService(
            VectorStore vectorStore,
            @Value("${rag.splitter.chunk-size:512}") int chunkSize,
            @Value("${rag.splitter.chunk-overlap:64}") int chunkOverlap,
            @Value("${rag.splitter.min-chunk-size:5}") int minChunkSize,
            @Value("${rag.splitter.max-num-chunks:10000}") int maxNumChunks) {
        this.vectorStore = vectorStore;
        this.splitter = new TokenTextSplitter(chunkSize, chunkOverlap, minChunkSize, maxNumChunks, true);
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
                .subscribe(vectorStore::add, error -> { throw new io.github.ragHub.core.exception.RagException("Batch ingest failed", error); });
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
