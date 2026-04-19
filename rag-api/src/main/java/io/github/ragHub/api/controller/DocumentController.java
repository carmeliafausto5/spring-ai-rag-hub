package io.github.ragHub.api.controller;

import io.github.ragHub.core.domain.KnowledgeDocument;
import io.github.ragHub.ingestion.adapter.FileIngestionAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final FileIngestionAdapter fileIngestionAdapter;

    public DocumentController(FileIngestionAdapter fileIngestionAdapter) {
        this.fileIngestionAdapter = fileIngestionAdapter;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {

        String docTitle = title != null ? title : file.getOriginalFilename();
        fileIngestionAdapter.ingestFile(file.getResource(), docTitle, Map.of("filename", file.getOriginalFilename()));
        return ResponseEntity.ok(Map.of("status", "ingested", "title", docTitle));
    }
}
