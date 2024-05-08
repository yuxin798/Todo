package com.todo.service;

import com.todo.entity.Message;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface RoomChatService {
    void sendMessage(Message message) throws IOException, TimeoutException;

    List<Message> receiveMessage(Long roomId);
}
