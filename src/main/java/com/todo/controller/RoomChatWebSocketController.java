package com.todo.controller;

import com.todo.entity.Message;
import com.todo.entity.User;
import com.todo.service.RoomChatService;
import com.todo.service.RoomService;
import com.todo.util.JwtUtil;
import com.todo.util.websocket.JsonDecoder;
import com.todo.util.websocket.JsonEncoder;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ServerEndpoint(
        value = "/roomchat/{roomId}",
        encoders = JsonEncoder.class,
        decoders = JsonDecoder.class
)
public class RoomChatWebSocketController {
    private static final ConcurrentHashMap<Long, Session> sessionMap = new ConcurrentHashMap<>();

    private static RoomService roomService;
    private static RoomChatService roomChatService;

    @Autowired
    public void roomChatWebSocketController(RoomService roomService, RoomChatService roomChatService) {
        RoomChatWebSocketController.roomService = roomService;
        RoomChatWebSocketController.roomChatService = roomChatService;
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
    public void onMessage(Session session, Message message) {
        log.info("【websocket消息】收到客户端消息: {}", message);
        broadCastMessage(session, message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误：{}", error.getMessage());
        error.printStackTrace();
    }

    /**
     * 发送消息 实践表明 每次浏览器刷新 session会发生变化
     */
    public static void sendMessage(Session session, Message message) {
        try {
            session.getBasicRemote().sendObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EncodeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 群发消息
     */
    public void broadCastMessage(Session session, Message message) {
        String roomId = session.getRequestParameterMap().get("roomId").get(0);

        String token = session.getRequestParameterMap().get("token").get(0);
        User u = JwtUtil.getUserByToken(token);
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(u, null, AuthorityUtils.NO_AUTHORITIES));

        roomService.listUsers(Long.valueOf(roomId)).forEach(user -> {
            Session s = sessionMap.get(user.getUserId());
            if (s != null) {
                sendMessage(s, message);
            } else {
                message.setToUserId(user.getUserId());
                roomChatService.sendMessage(message);
            }
        });
    }

    // /**
    //  * 指定Session发送消息
    //  *
    //  * @param sessionId
    //  * @param message
    //  * @throws IOException
    //  */
    // public static void SendMessage(String message, String sessionId) throws IOException {
    //     Session session = null;
    //     for (Session s : SessionSet) {
    //         if (s.getId().equals(sessionId)) {
    //             session = s;
    //             break;
    //         }
    //     }
    //     if (session != null)
    //         SendMessage(session, message);
    // }
}
