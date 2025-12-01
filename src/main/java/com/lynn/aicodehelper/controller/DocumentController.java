package com.lynn.aicodehelper.controller;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@RestController
@RequestMapping("/doc")
public class DocumentController {

    @Resource
    private EmbeddingStoreIngestor embeddingStoreIngestor;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            return ResponseEntity.badRequest().body("invalid file name");
        }
        String lower = originalName.toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".md") || lower.endsWith(".txt"))) {
            return ResponseEntity.badRequest().body("only .md or .txt supported");
        }

        Path temp = Files.createTempFile("rag-upload-", lower.endsWith(".md") ? ".md" : ".txt");
        Files.write(temp, file.getBytes());

        Document document = FileSystemDocumentLoader.loadDocument(temp);

        embeddingStoreIngestor.ingest(document);

        return ResponseEntity.ok("ok");
    }
}
