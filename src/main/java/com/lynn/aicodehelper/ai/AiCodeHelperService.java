package com.lynn.aicodehelper.ai;

import com.lynn.aicodehelper.ai.guardrail.SafeInputGuardrail;
import com.lynn.aicodehelper.ai.tools.InterviewQuestionTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.service.*;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.spring.AiService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

//@AiService
@Service
@InputGuardrails({SafeInputGuardrail.class})
public interface AiCodeHelperService {

    @SystemMessage(fromResource = "/prompt/SystemPrompt.txt")
    String chat(String userMessage);

    //学习报告
    record Report(String name , List<String> suggestionList){}

    @SystemMessage(fromResource = "/prompt/SystemPrompt.txt")
    Report chatForReport(String userMessage);

    @SystemMessage(fromResource = "/prompt/SystemPrompt.txt")
    Result<String> chatWithRag(String userMessage);

    // 流式对话
    @SystemMessage(fromResource = "/prompt/SystemPrompt.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);

}