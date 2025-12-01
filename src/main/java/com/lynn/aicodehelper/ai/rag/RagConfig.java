package com.lynn.aicodehelper.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import java.util.Objects;

import java.util.List;

/**
 * ClassName: RagConfig
 * Description:
 *
 * @Author linz
 * @Creat 2025/9/23 09:03
 * @Version 1.00
 */
@Configuration
public class RagConfig {
    @Value("${rag.ingest-on-startup:true}")
    private boolean ingestOnStartup;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public DocumentByCharacterSplitter documentSplitter() {
        return new DocumentByCharacterSplitter(1000, 200);
    }

    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(EmbeddingModel embeddingModel,
                                                         EmbeddingStore<TextSegment> embeddingStore,
                                                         DocumentByCharacterSplitter documentSplitter) {
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .textSegmentTransformer(textSegment -> TextSegment.from(
                        textSegment.metadata().getString("file_name") + "\n" + textSegment.text(),
                        textSegment.metadata()))
                .embeddingStore(embeddingStore)
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingModel embeddingModel,
                                            EmbeddingStore<TextSegment> embeddingStore,
                                            EmbeddingStoreIngestor ingestor,
                                            DocumentRegistry registry) {

        // 可选：启动时加载内置文档
        if (ingestOnStartup) {
            List<Document> documentList = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");
            ingestor.ingest(documentList);
            registry.addAll(documentList.stream()
                    .map(d -> d.metadata().getString("file_name"))
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList());
        }
        // 4.自定义内容加载器
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(5) //最多返回5个结果
                .minScore(0.75) //过滤分数小于0.75的结果
                .build();
        return contentRetriever;
    }
}
