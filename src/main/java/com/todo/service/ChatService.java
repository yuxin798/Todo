package com.todo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.todo.entity.Message;
import com.todo.service.impl.ChatServiceImplDelegator;

import java.util.Date;
import java.util.List;

public interface ChatService extends IService<Message> {
    void sendMessage(Message message, ChatServiceImplDelegator.MessageType type);

    List<Message> receiveMessage(Long fromId, ChatServiceImplDelegator.MessageType type);

    Page<Message> findMessagePage(Long otherId, Date beforeDateTime, int pageNum, int pageSize, ChatServiceImplDelegator.MessageType type);
}
