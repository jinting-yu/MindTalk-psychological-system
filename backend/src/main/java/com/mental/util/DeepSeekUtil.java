package com.mental.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekUtil {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(String message) {
        System.out.println("收到AI聊天请求: " + message);
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", getMessages(message));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            // 发送请求
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String apiUrl = "https://api.deepseek.com/v1/chat/completions";
            System.out.println("正在调用DeepSeek API...");
            System.out.println("API Key: " + apiKey);
            System.out.println("URL: " + apiUrl);
            System.out.println("请求体: " + requestBody);

            // 增加超时设置
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {
                {
                    setConnectTimeout(10000); // 10秒连接超时
                    setReadTimeout(30000); // 30秒读取超时
                }
            });

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // 解析响应
            System.out.println("API调用状态码: " + response.getStatusCode());
            System.out.println("响应体: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("API调用成功");
                Map<String, Object> body = response.getBody();

                // 检查是否有错误信息
                if (body.containsKey("error")) {
                    Map<String, Object> error = (Map<String, Object>) body.get("error");
                    String errorMessage = (String) error.get("message");
                    System.out.println("API错误: " + errorMessage);
                    return "抱歉，API调用失败: " + errorMessage;
                }

                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                    if (messageObj != null) {
                        String content = (String) messageObj.get("content");
                        System.out.println("AI响应: " + content);
                        return content;
                    }
                }
            } else {
                System.out.println("API调用失败，状态码: " + response.getStatusCode());
                return "抱歉，API调用失败，状态码: " + response.getStatusCode();
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.out.println("HTTP客户端错误: " + e.getStatusCode() + " - " + e.getStatusText());
            System.out.println("错误响应体: " + e.getResponseBodyAsString());
            return "抱歉，API调用失败: " + e.getStatusText();
        } catch (Exception e) {
            System.out.println("API调用异常: " + e.getMessage());
            e.printStackTrace();
        }

        return "抱歉，我遇到了一点问题，请稍后再试~";
    }

    private List<Map<String, Object>> getMessages(String userMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();

        // 系统消息
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的心理健康助手，专注于提供情感支持和心理健康建议。请以温暖、理解和专业的态度回应用户的问题。");
        messages.add(systemMessage);

        // 用户消息
        Map<String, Object> userMessageObj = new HashMap<>();
        userMessageObj.put("role", "user");
        userMessageObj.put("content", userMessage);
        messages.add(userMessageObj);

        return messages;
    }
}
