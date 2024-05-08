package com.todo.service;

import com.todo.entity.Message;

import java.util.List;

public interface RoomChatService {
    void sendMessage(Message message);

    List<Message> receiveMessage(Long roomId);
}
