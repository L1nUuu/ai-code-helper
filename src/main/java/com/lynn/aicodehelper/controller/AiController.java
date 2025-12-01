package com.lynn.aicodehelper.controller;

import com.lynn.aicodehelper.ai.AiCodeHelperService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource(name = "aiCodeHelperService")
    private AiCodeHelperService aiCodeHelperServiceRag;
    @Resource(name = "aiCodeHelperServicePlain")
    private AiCodeHelperService aiCodeHelperServicePlain;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(int memoryId, String message, boolean useRag) {
        AiCodeHelperService svc = useRag ? aiCodeHelperServiceRag : aiCodeHelperServicePlain;
        return svc.chatStream(memoryId, message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/chat-alt")
    public Flux<ServerSentEvent<String>> chatAlt(int memoryId, String message, boolean useRag) {
        AiCodeHelperService svc = useRag ? aiCodeHelperServiceRag : aiCodeHelperServicePlain;
        return svc.chatStreamCompare(memoryId, message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}
