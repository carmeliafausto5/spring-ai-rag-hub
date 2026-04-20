package io.github.ragHub.ingestion.adapter;

import io.github.ragHub.core.domain.KnowledgeDocument;
import io.github.ragHub.core.port.FileIngestionPort;
import io.github.ragHub.ingestion.service.IngestionService;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FileIngestionAdapter implements FileIngestionPort {

    private final IngestionService ingestionService;

    public FileIngestionAdapter(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void ingestFile(Resource resource, String title, Map<String, Object> metadata) {
        var reader = new TikaDocumentReader(resource);
        String content = reader.get().stream()
                .map(doc -> doc.getText())
                .reduce("", (a, b) -> a + "\n" + b);

        ingestionService.ingest(KnowledgeDocument.of(title, content, resource.getFilename(), metadata));
    }
}
