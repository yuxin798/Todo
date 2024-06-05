package com.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.todo.dto.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName(value ="chat_message")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;
    private Long fromUserId;
    private Long toUserId;
    private Long toRoomId;
    private String content;
    private Date sendTime;
    private Date createdAt;
    private Date updatedAt;
    private Integer deleted;

    public Message(MessageDto messageDto) {
        this.fromUserId = messageDto.getFromUserId();
        this.toUserId = messageDto.getToUserId();
        this.toRoomId = messageDto.getToRoomId();
        this.content = messageDto.getContent();
        this.setSendTime(new Date());
    }
}
