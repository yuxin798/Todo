package com.todo.controller;

import com.todo.entity.Message;
import com.todo.service.impl.RoomChatServiceImpl;
import com.todo.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Tag(name = "聊天API")
@RestController("/chat/room")
public class RoomChatController {
    private final RoomChatServiceImpl roomChatServiceImpl;

    public RoomChatController(RoomChatServiceImpl roomChatServiceImpl) {
        this.roomChatServiceImpl = roomChatServiceImpl;
    }

    // 用 websocket 发送消息
    // @Operation(summary = "发送消息")
    // @PostMapping("/send")
    // public Result<?> sendMessage(@RequestBody Message message) {
    //     roomChatServiceImpl.sendMessage(message);
    //     return Result.success();
    // }

    @Operation(summary = "接收消息")
    @GetMapping("/receive")
    public Result<?> receiveMessage(Long roomId) {
        List<Message> message = roomChatServiceImpl.receiveMessage(roomId);
        return Result.success(message);
    }
}
