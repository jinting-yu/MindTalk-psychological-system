package com.mental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mental.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Update("UPDATE chat_message SET is_read = 1 WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND is_read = 0")
    void markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
