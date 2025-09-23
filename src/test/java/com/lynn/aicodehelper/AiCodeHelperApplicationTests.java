package com.lynn.aicodehelper;

import com.alibaba.dashscope.threads.ContentText;
import com.lynn.aicodehelper.ai.AiCodeHelper;
import com.lynn.aicodehelper.ai.AiCodeHelperService;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeHelperApplicationTests {

    @Resource
    private AiCodeHelper aiCodeHelper;

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @Test
    void contextLoads() {
    }

    @Test
    public void chatTest(){
        String response = aiCodeHelper.chat("写一个java程序，求1+2+3+4+5+6+7+8+9+10,不超过20行代码");
        System.out.println(response);
    }

    @Test
    void chatWithMessage() {
        UserMessage message = UserMessage.from(
                TextContent.from("请描述一下这个图片"),
                ImageContent.from("www.codefather.cn/logo.png")
        );

        String response = aiCodeHelper.chatWithMessage(message);
        System.out.println( response);
    }

    @Test
    void chatWithPrompt() {
        String response = aiCodeHelper.chatWithPrompt("你好，我是Lynn");
        System.out.println( response);

    }

    @Test
    public void chatWithAiCodeHelperService(){
        String result = aiCodeHelperService.chat("你好，我是程序员LYnn");
        System.out.println(result);
    }

    @Test
    public void chatWithMemory(){
        String result = aiCodeHelperService.chat("你好，我是程序员LYnn");
        System.out.println(result);
        result = aiCodeHelperService.chat("我是谁来着");
        System.out.println(result);
    }

    @Test
    public void chatForReport(){
        AiCodeHelperService.Report report = aiCodeHelperService
                .chatForReport("你好，我是程序员LYnn，请给我一份Java的学习报告。");
        System.out.println(report);
    }
}
