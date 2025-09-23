package com.lynn.aicodehelper.ai;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import org.springframework.stereotype.Service;

import java.util.List;

//@AiService
@Service
public interface AiCodeHelperService {

    @SystemMessage(fromResource = "/prompt/SystemPrompt.txt")
    String chat(String userMessage);

    //学习报告
    record Report(String name , List<String> suggestionList){}

    @SystemMessage(fromResource = "/prompt/SystemPrompt.txt")
    Report chatForReport(String userMessage);

    Result<String> chatWithRag(String userMessage);

}