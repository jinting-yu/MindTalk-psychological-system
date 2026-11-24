package com.mental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mental.entity.ChatMessage;
import com.mental.entity.ChatSession;
import java.util.List;
import java.util.Map;

public interface ChatService extends IService<ChatMessage> {

    // 创建或获取会话
    ChatSession getOrCreateSession(Long userId, Long counselorId);

    // 发送消息
    ChatMessage sendMessage(Long senderId, Long receiverId, String content, String senderRole, String receiverRole);

    // 获取会话消息列表
    List<ChatMessage> getMessagesBySession(Long sessionId, Integer pageNum, Integer pageSize);

    // 获取用户的所有会话
    List<Map<String, Object>> getUserSessions(Long userId);

    // 获取咨询师的所有会话
    List<Map<String, Object>> getCounselorSessions(Long counselorId);

    // 标记消息已读
    void markMessagesAsRead(Long sessionId, Long userId);

    // 获取未读消息数
    Integer getUnreadCount(Long userId);
}
