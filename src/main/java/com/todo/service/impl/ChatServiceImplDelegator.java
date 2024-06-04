package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.entity.Message;
import com.todo.mapper.ChatMapper;
import com.todo.mapper.UserMapper;
import com.todo.service.ChatService;
import com.todo.util.UserContextUtil;
import com.todo.vo.MessageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ChatServiceImplDelegator extends ServiceImpl<ChatMapper, Message> implements ChatService {
    private final RoomChatServiceImpl roomChatService;
    private final UserChatServiceImpl userChatService;

    private final UserMapper userMapper;

    @Autowired
    public ChatServiceImplDelegator(RoomChatServiceImpl roomChatService, UserChatServiceImpl userChatService, UserMapper userMapper) {
        this.roomChatService = roomChatService;
        this.userChatService = userChatService;
        this.userMapper = userMapper;
    }

    @Override
    public void sendMessage(Message message, MessageType type) {
        switch (type) {
            case TO_USER -> userChatService.sendMessage(message);
            case TO_ROOM -> roomChatService.sendMessage(message);
        }
    }

    @Override
    public List<MessageVo> receiveMessage(Long fromId, MessageType type) {
        List<MessageVo> messages = null;
        switch (type) {
            case TO_USER -> messages = userChatService.receiveMessage(fromId);
            case TO_ROOM -> messages = roomChatService.receiveMessage(fromId);
        }

        return Objects.requireNonNullElseGet(messages, ArrayList::new);
    }

    @Override
    public Page<MessageVo> findMessagePage(Long otherId, Date beforeDateTime, int pageNum, int pageSize, MessageType type) {
        Long userId = UserContextUtil.getUser().getUserId();
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>(Message.class)
                .lt(Message::getSendTime, beforeDateTime)
                // 用户间的消息
                .and(type == MessageType.TO_USER, w -> w
                        .eq(Message::getFromUserId, userId)
                        .eq(Message::getToUserId, otherId)
                    .or()
                        .eq(Message::getFromUserId, otherId)
                        .eq(Message::getToUserId, userId))
                // 自习室间的消息
                .and(type == MessageType.TO_ROOM, w -> w
                        .eq(Message::getToRoomId, otherId))
                .orderByDesc(Message::getSendTime);
        Page<Message> messagePage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<MessageVo> messageVos = messagePage.getRecords()
                .stream()
                .map(MessageVo::new)
                .peek(messageVo -> {
                    messageVo.setFromUserName(userMapper.selectById(messageVo.getFromUserId()).getUserName());
                    messageVo.setFromUserAvatar(userMapper.selectById(messageVo.getFromUserId()).getAvatar());
                })
                .toList();

        Page<MessageVo> messageVoPage = new Page<>();
        messageVoPage.setRecords(messageVos);
        messageVoPage.setSize(messageVos.size());
        messageVoPage.setTotal(messagePage.getTotal());
        messageVoPage.setCurrent(messagePage.getCurrent());

        return messageVoPage;
    }

    public boolean save(Message entity, MessageType type) {
        entity.setMessageId(null);
        switch (type) {
            case TO_USER -> entity.setToRoomId(null);
            case TO_ROOM -> entity.setToUserId(null);
        }
        return super.save(entity);
    }

    public enum MessageType {
        TO_ROOM, TO_USER
    }
}
