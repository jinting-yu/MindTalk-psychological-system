package com.mental.controller;

import com.mental.common.Result;
import com.mental.util.MentalAIFloatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AIFloatController {

    private static final Logger logger = LoggerFactory.getLogger(AIFloatController.class);

    @Autowired
    private MentalAIFloatUtil mentalAIFloatUtil;

    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, String> params) {
        String message = params.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error("消息不能为空");
        }
        logger.info("收到AI聊天请求，消息内容: {}", message);
        String reply = mentalAIFloatUtil.chat(message);
        logger.info("AI回复内容: {}", reply);
        return Result.success(reply);
    }
}