package io.github.ragHub.api.controller;

import io.github.ragHub.ingestion.adapter.FileIngestionAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/html", "text/markdown", "text/plain"
    );

    private final FileIngestionAdapter fileIngestionAdapter;

    public DocumentController(FileIngestionAdapter fileIngestionAdapter) {
        this.fileIngestionAdapter = fileIngestionAdapter;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        String docTitle = title != null ? title : filename;
        fileIngestionAdapter.ingestFile(file.getResource(), docTitle, Map.of("filename", filename));
        return ResponseEntity.ok(Map.of("status", "ingested", "title", docTitle));
    }
}
