package com.mental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mental.entity.ChatMessage;
import com.mental.entity.ChatSession;
import com.mental.entity.User;
import com.mental.mapper.ChatMessageMapper;
import com.mental.mapper.ChatSessionMapper;
import com.mental.service.ChatService;
import com.mental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public ChatSession getOrCreateSession(Long userId, Long counselorId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getCounselorId, counselorId)
                .eq(ChatSession::getStatus, "active");

        ChatSession session = chatSessionMapper.selectOne(wrapper);

        if (session == null) {
            session = new ChatSession();
            session.setUserId(userId);
            session.setCounselorId(counselorId);
            session.setStatus("active");
            session.setUnreadCount(0);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            chatSessionMapper.insert(session);
        }

        return session;
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content, String senderRole, String receiverRole) {
        Long userId = "USER".equals(senderRole) ? senderId : receiverId;
        Long counselorId = "COUNSELOR".equals(senderRole) ? receiverId : senderId;
        ChatSession session = getOrCreateSession(userId, counselorId);

        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setSenderRole(senderRole);
        message.setReceiverRole(receiverRole);
        message.setContent(content);
        message.setMessageType(0);
        message.setIsRead(0);
        message.setIsDeleted(0);
        message.setCreateTime(LocalDateTime.now());

        this.save(message);

        session.setUnreadCount(session.getUnreadCount() + 1);
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);

        return message;
    }

    @Override
    public List<ChatMessage> getMessagesBySession(Long sessionId, Integer pageNum, Integer pageSize) {
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreateTime);

        Page<ChatMessage> result = this.page(page, wrapper);
        return result.getRecords();
    }

    @Override
    public List<Map<String, Object>> getUserSessions(Long userId) {
        List<Map<String, Object>> sessions = new ArrayList<>();

        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getStatus, "active")
                .orderByDesc(ChatSession::getUpdateTime);

        List<ChatSession> sessionList = chatSessionMapper.selectList(wrapper);

        for (ChatSession session : sessionList) {
            User counselor = userService.getById(session.getCounselorId());
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("sessionId", session.getId());
            sessionInfo.put("counselorId", counselor.getId());
            sessionInfo.put("counselorName", counselor.getUsername());  // 改为 getUsername()
            sessionInfo.put("unreadCount", session.getUnreadCount());
            sessionInfo.put("updateTime", session.getUpdateTime());

            // 获取最后一条消息
            LambdaQueryWrapper<ChatMessage> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(ChatMessage::getSessionId, session.getId())
                    .orderByDesc(ChatMessage::getCreateTime)
                    .last("LIMIT 1");
            ChatMessage lastMsg = this.getOne(msgWrapper);
            if (lastMsg != null) {
                sessionInfo.put("lastMessage", lastMsg.getContent());
                sessionInfo.put("lastMessageTime", lastMsg.getCreateTime());
            }

            sessions.add(sessionInfo);
        }

        return sessions;
    }

    @Override
    public List<Map<String, Object>> getCounselorSessions(Long counselorId) {
        List<Map<String, Object>> sessions = new ArrayList<>();

        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getCounselorId, counselorId)
                .eq(ChatSession::getStatus, "active")
                .orderByDesc(ChatSession::getUpdateTime);

        List<ChatSession> sessionList = chatSessionMapper.selectList(wrapper);

        for (ChatSession session : sessionList) {
            User user = userService.getById(session.getUserId());
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("sessionId", session.getId());
            sessionInfo.put("userId", user.getId());
            sessionInfo.put("userName", user.getUsername());  // 改为 getUsername()
            sessionInfo.put("unreadCount", session.getUnreadCount());
            sessionInfo.put("updateTime", session.getUpdateTime());

            LambdaQueryWrapper<ChatMessage> msgWrapper = new LambdaQueryWrapper<>();
            msgWrapper.eq(ChatMessage::getSessionId, session.getId())
                    .orderByDesc(ChatMessage::getCreateTime)
                    .last("LIMIT 1");
            ChatMessage lastMsg = this.getOne(msgWrapper);
            if (lastMsg != null) {
                sessionInfo.put("lastMessage", lastMsg.getContent());
                sessionInfo.put("lastMessageTime", lastMsg.getCreateTime());
            }

            sessions.add(sessionInfo);
        }

        return sessions;
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long sessionId, Long userId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getIsRead, 0);

        ChatMessage message = new ChatMessage();
        message.setIsRead(1);
        this.update(message, wrapper);

        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session != null) {
            session.setUnreadCount(0);
            chatSessionMapper.updateById(session);
        }
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getStatus, "active");
        List<ChatSession> sessions = chatSessionMapper.selectList(wrapper);
        return sessions.stream().mapToInt(ChatSession::getUnreadCount).sum();
    }
}