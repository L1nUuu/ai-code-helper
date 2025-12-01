package com.lynn.aicodehelper.controller;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import com.lynn.aicodehelper.ai.rag.DocumentRegistry;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    @Resource
    private DocumentRegistry documentRegistry;
    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

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
        documentRegistry.add(originalName);

        return ResponseEntity.ok("ok");
    }

    @GetMapping("/list")
    public ResponseEntity<Object> list() {
        return ResponseEntity.ok(documentRegistry.list());
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clear() {
        embeddingStore.removeAll();
        documentRegistry.clear();
        return ResponseEntity.ok("ok");
    }
}
