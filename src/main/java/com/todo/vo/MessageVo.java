package com.todo.vo;

import com.todo.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageVo {
    private Long messageId;
    private Long fromUserId;
    private Long toUserId;
    private Long toRoomId;
    private String content;
    private Integer type;
    private Date sendTime;
    private String fromUserName;
    private String fromUserAvatar;

    public MessageVo(Message message) {
        this.messageId = message.getMessageId();
        this.fromUserId = message.getFromUserId();
        this.toUserId = message.getToUserId();
        this.toRoomId = message.getToRoomId();
        this.content = message.getContent();
        this.type = message.getType();
        this.sendTime = message.getSendTime();
    }
}
