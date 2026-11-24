package com.mental.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;

    private Long senderId;      // 发送者ID
    private Long receiverId;    // 接收者ID
    private String senderRole;  // 发送者角色：USER/COUNSELOR/TEACHER
    private String receiverRole;// 接收者角色
    private String content;     // 消息内容
    private Integer messageType;// 消息类型：0-文本，1-图片，2-语音
    private Integer isRead;     // 是否已读：0-未读，1-已读
    private Integer isDeleted;  // 是否删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}