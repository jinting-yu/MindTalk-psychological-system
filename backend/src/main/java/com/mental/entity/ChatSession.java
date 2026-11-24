package com.mental.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_session")
public class ChatSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;        // 用户ID
    private Long counselorId;   // 咨询师ID
    private String status;      // 状态：active-进行中，closed-已关闭
    private Integer unreadCount;// 未读消息数

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}