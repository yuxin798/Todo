package com.todo.controller;

import com.todo.entity.Message;
import com.todo.service.UserChatService;
import com.todo.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "用户聊天API")
@RestController
@RequestMapping("/chat/user")
public class UserChatController {
    private final UserChatService userChatService;

    public UserChatController(UserChatService userChatService) {
        this.userChatService = userChatService;
    }

    // // 用 websocket 发送消息
    // @Operation(summary = "发送消息")
    // @PostMapping("/send")
    // public Result<?> sendMessage(@RequestBody Message message) {
    //     userChatService.sendMessage(message);
    //     return Result.success();
    // }

    @Operation(summary = "接收消息")
    @GetMapping("/receive")
    public Result<?> receiveMessage(Long fromUserId) {
        List<Message> message = userChatService.receiveMessage(fromUserId);
        return Result.success(message);
    }
}
