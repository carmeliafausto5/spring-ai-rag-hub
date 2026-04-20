package io.github.ragHub.ingestion.service;

import io.github.ragHub.core.domain.KnowledgeDocument;
import io.github.ragHub.core.port.DocumentIngestionPort;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class IngestionService implements DocumentIngestionPort {

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;
    private final JdbcTemplate jdbc;

    public IngestionService(
            VectorStore vectorStore,
            JdbcTemplate jdbc,
            @Value("${rag.splitter.chunk-size:512}") int chunkSize,
            @Value("${rag.splitter.chunk-overlap:64}") int chunkOverlap,
            @Value("${rag.splitter.min-chunk-size:5}") int minChunkSize,
            @Value("${rag.splitter.max-num-chunks:10000}") int maxNumChunks) {
        this.vectorStore = vectorStore;
        this.jdbc = jdbc;
        this.splitter = new TokenTextSplitter(chunkSize, chunkOverlap, minChunkSize, maxNumChunks, true);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void ingest(KnowledgeDocument doc) {
        jdbc.update("DELETE FROM vector_store WHERE metadata->>'documentId' = ?", doc.id());
        List<Document> chunks = splitter.apply(List.of(toSpringDoc(doc)));
        vectorStore.add(chunks);
    }

    @Override
    public void ingestBatch(Flux<KnowledgeDocument> documents) {
        try {
            List<Document> chunks = documents.collectList().block()
                    .stream().map(this::toSpringDoc).toList();
            vectorStore.add(splitter.apply(chunks));
        } catch (Exception e) {
            throw new io.github.ragHub.core.exception.RagException("Batch ingest failed", e);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void delete(String documentId) {
        jdbc.update("DELETE FROM vector_store WHERE metadata->>'documentId' = ?", documentId);
    }

    private Document toSpringDoc(KnowledgeDocument doc) {
        Map<String, Object> meta = new java.util.HashMap<>(doc.metadata());
        meta.put("documentId", doc.id());
        meta.put("title", doc.title());
        meta.put("sourceUri", doc.sourceUri());
        return new Document(doc.id(), doc.content(), meta);
    }
}
