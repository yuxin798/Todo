package com.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private Long fromUserId;
    private Long toUserId;
    private Long toRoomId;
    private String content;
}
