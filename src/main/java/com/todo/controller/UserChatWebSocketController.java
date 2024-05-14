package com.todo.controller;

import com.todo.dto.MessageDto;
import com.todo.entity.Message;
import com.todo.entity.User;
import com.todo.service.impl.ChatServiceImplDelegator;
import com.todo.service.impl.ChatServiceImplDelegator.MessageType;
import com.todo.util.JwtUtil;
import com.todo.util.websocket.JsonMessageDecoder;
import com.todo.util.websocket.JsonMessageDtoDecoder;
import com.todo.util.websocket.JsonMessageDtoEncoder;
import com.todo.util.websocket.JsonMessageEncoder;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ServerEndpoint(
        value = "/userchat/{toUserId}",
        encoders = { JsonMessageDtoEncoder.class, JsonMessageEncoder.class },
        decoders = { JsonMessageDtoDecoder.class, JsonMessageDecoder.class }
)
public class UserChatWebSocketController {
    private static final ConcurrentHashMap<Long, Session> sessionMap = new ConcurrentHashMap<>();

    private static ChatServiceImplDelegator chatServiceDelegator;

    @Autowired
    public void roomChatWebSocketController(ChatServiceImplDelegator chatServiceDelegator) {
        UserChatWebSocketController.chatServiceDelegator = chatServiceDelegator;
    }

    @OnOpen
    public void onOpen(Session session) {
        String token = session.getRequestParameterMap().get("token").get(0);
        Long userId = JwtUtil.getUserByToken(token).getUserId();
        sessionMap.put(userId, session);
    }

    @OnClose
    public void onClose(Session session) {
        String token = session.getRequestParameterMap().get("token").get(0);
        Long userId = JwtUtil.getUserByToken(token).getUserId();
        sessionMap.remove(userId);
    }

    @OnMessage
    public void onMessage(Session fromSession, MessageDto messageDto) {
        Message message = new Message(messageDto);
        log.info("收到客户端消息: {}", message);
        String token = fromSession.getRequestParameterMap().get("token").get(0);
        User u = JwtUtil.getUserByToken(token);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(u, null, AuthorityUtils.NO_AUTHORITIES));
        message.setFromUserId(u.getUserId());
        // 持久化
        chatServiceDelegator.save(message, MessageType.TO_USER);
        // 发送
        sendMessage(fromSession, sessionMap.get(message.getToUserId()), message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误：{}", error.getMessage());
        error.printStackTrace();
    }

    public void sendMessage(Session fromSession, Session toSession, Message message) {
        if (toSession == null) {
            // by rabbitmq
            chatServiceDelegator.sendMessage(message, MessageType.TO_USER);
            return;
        }
        try {
            // by websocket
            fromSession.getBasicRemote().sendObject(message);
            toSession.getBasicRemote().sendObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EncodeException e) {
            throw new RuntimeException(e);
        }
    }
}
