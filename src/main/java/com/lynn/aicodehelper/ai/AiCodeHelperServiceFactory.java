package com.lynn.aicodehelper.ai;

import com.lynn.aicodehelper.ai.tools.InterviewQuestionTool;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: AiCodeHelpServiceFactory
 * Description:
 *
 * @Author linz
 * @Creat 2025/9/22 16:57
 * @Version 1.00
 */

@Configuration
public class AiCodeHelperServiceFactory {
    @Resource
    private ChatModel qwenChatModel;
    @Resource
    private ContentRetriever contentRetriever;

    @Bean
    public AiCodeHelperService createAiCodeHelperService(){
        // 回话记忆
        ChatMemory chatMemory= MessageWindowChatMemory.withMaxMessages(10);
        // 构建 ai service
        AiCodeHelperService aiCodeHelperService = AiServices
                .builder(AiCodeHelperService.class)
                .chatModel(qwenChatModel)
                .chatMemory(chatMemory)
                .contentRetriever(contentRetriever) //RAG检索增强生成
                .tools(new InterviewQuestionTool()) //工具调用
                .build();

        return aiCodeHelperService;
    }
}
