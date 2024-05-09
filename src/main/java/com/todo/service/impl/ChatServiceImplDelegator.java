package com.todo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.entity.Message;
import com.todo.mapper.ChatMapper;
import com.todo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ChatServiceImplDelegator extends ServiceImpl<ChatMapper, Message> implements ChatService {
    private final RoomChatServiceImpl roomChatService;
    private final UserChatServiceImpl userChatService;

    @Autowired
    public ChatServiceImplDelegator(RoomChatServiceImpl roomChatService, UserChatServiceImpl userChatService) {
        this.roomChatService = roomChatService;
        this.userChatService = userChatService;
    }

    @Override
    public void sendMessage(Message message, MessageType type) {
        message.setMessageId(null);
        message.setToRoomId(null);
        switch (type) {
            case TO_USER -> userChatService.sendMessage(message);
            case TO_ROOM -> roomChatService.sendMessage(message);
        }
    }

    @Override
    public List<Message> receiveMessage(Long fromId, MessageType type) {
        List<Message> messages = null;
        switch (type) {
            case TO_USER -> messages = userChatService.receiveMessage(fromId);
            case TO_ROOM -> messages = roomChatService.receiveMessage(fromId);
        }

        return Objects.requireNonNullElseGet(messages, ArrayList::new);
    }

    public enum MessageType {
        TO_ROOM, TO_USER
    }
}
