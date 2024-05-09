package com.todo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName(value ="chat_message")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @TableId
    private Long messageId;
    private Long fromUserId;
    private Long toUserId;
    private Long toRoomId;
    private String content;
    private Date sendTime;
    private Date createdAt;
    private Date updatedAt;
    private Integer deleted;
}
