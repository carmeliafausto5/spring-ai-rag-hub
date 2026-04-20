package io.github.ragHub.ingestion;

import io.github.ragHub.core.domain.KnowledgeDocument;
import io.github.ragHub.ingestion.service.IngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class IngestionServiceTest {

    private IngestionService service(VectorStore vs) {
        return new IngestionService(vs, 512, 64, 5, 10000);
    }

    @Test
    void ingest_splitsAndStoresDocument() {
        var vectorStore = mock(VectorStore.class);
        var doc = KnowledgeDocument.of("Test Doc", "Hello world. ".repeat(100), "file://test.txt", Map.of());
        service(vectorStore).ingest(doc);
        verify(vectorStore, atLeastOnce()).add(anyList());
    }

    @Test
    void delete_removesFromVectorStore() {
        var vectorStore = mock(VectorStore.class);
        service(vectorStore).delete("doc-123");
        verify(vectorStore).delete(List.of("doc-123"));
    }
}
