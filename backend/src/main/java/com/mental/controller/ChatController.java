package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.common.Result;
import com.mental.entity.ChatMessage;
import com.mental.entity.ChatSession;
import com.mental.entity.User;
import com.mental.security.UserContext;
import com.mental.service.ChatService;
import com.mental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    /**
     * 获取或创建会话
     */
    @PostMapping("/session")
    public Result<ChatSession> getOrCreateSession(@RequestBody Map<String, Long> params) {
        Long userId = UserContext.getUserId();
        Long counselorId = params.get("counselorId");

        // 验证咨询师是否存在且角色正确
        User counselor = userService.getById(counselorId);
        if (counselor == null || (!"COUNSELOR".equals(counselor.getRole()) && !"TEACHER".equals(counselor.getRole()))) {
            return Result.error("咨询师不存在");
        }

        ChatSession session = chatService.getOrCreateSession(userId, counselorId);
        return Result.success(session);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<ChatMessage> sendMessage(@RequestBody Map<String, Object> params) {
        Long senderId = UserContext.getUserId();
        String senderRole = UserContext.getRole();
        Long receiverId = Long.valueOf(params.get("receiverId").toString());
        String content = params.get("content").toString();
        String receiverRole = params.get("receiverRole").toString();

        ChatMessage message = chatService.sendMessage(senderId, receiverId, content, senderRole, receiverRole);
        return Result.success(message);
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/messages")
    public Result<Map<String, Object>> getMessages(
            @RequestParam Long sessionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        List<ChatMessage> messages = chatService.getMessagesBySession(sessionId, pageNum, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("total", messages.size());

        return Result.success(result);
    }

    /**
     * 获取用户的会话列表
     */
    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> getUserSessions() {
        Long userId = UserContext.getUserId();
        String userRole = UserContext.getRole();

        if ("USER".equals(userRole)) {
            return Result.success(chatService.getUserSessions(userId));
        } else if ("COUNSELOR".equals(userRole) || "TEACHER".equals(userRole)) {
            return Result.success(chatService.getCounselorSessions(userId));
        }

        return Result.success(Collections.emptyList());  // 改为 Collections.emptyList()
    }

    /**
     * 标记消息已读
     */
    @PutMapping("/read")
    public Result<Void> markAsRead(@RequestParam Long sessionId) {
        Long userId = UserContext.getUserId();
        chatService.markMessagesAsRead(sessionId, userId);
        return Result.success(null);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = UserContext.getUserId();
        Integer count = chatService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 获取可咨询的咨询师列表
     */
    @GetMapping("/counselors")
    public Result<List<User>> getCounselors() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getRole, "COUNSELOR", "TEACHER");
        List<User> counselors = userService.list(wrapper);
        return Result.success(counselors);
    }
}