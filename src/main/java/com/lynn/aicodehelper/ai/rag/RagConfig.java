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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

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
    public ContentRetriever contentRetriever(EmbeddingModel embeddingModel,
                                            EmbeddingStore<TextSegment> embeddingStore) {
        //RAG
        // 2.文档分割，每个文档按照段落进行分割，最大1000个字符，每次最多重叠200个字符
        DocumentByCharacterSplitter documentByCharacterSplitter =
                new DocumentByCharacterSplitter(1000, 200);
        // 3.自定义文档加载器，把文档转换成向量存储到向量数据库中
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentByCharacterSplitter)
                .embeddingModel(embeddingModel)
                //为了提高文档的质量，为每一个切割后的文档碎片 TextSegment 添加文档名称作为元信息
                .textSegmentTransformer(textSegment -> {
                    return TextSegment.from(
                            textSegment.metadata().getString("file_name")
                                    + "\n" + textSegment.text(), textSegment.metadata());
                })
                .embeddingStore(embeddingStore)
                .build();

        // 可选：启动时加载内置文档
        if (ingestOnStartup) {
            List<Document> documentList = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");
            // 加载文档
            ingestor.ingest(documentList);
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
