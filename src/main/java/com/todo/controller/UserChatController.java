package com.todo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.todo.entity.Message;
import com.todo.service.impl.ChatServiceImplDelegator;
import com.todo.service.impl.ChatServiceImplDelegator.MessageType;
import com.todo.vo.MessageVo;
import com.todo.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@Tag(name = "用户聊天API")
@RestController
@RequestMapping("/chat/user")
public class UserChatController {
    private final ChatServiceImplDelegator chatServiceDelegator;

    public UserChatController(ChatServiceImplDelegator chatServiceDelegator) {
        this.chatServiceDelegator = chatServiceDelegator;
    }


    @Operation(summary = "接收消息")
    @GetMapping("/receive")
    public Result<?> receiveMessage(Long fromUserId) {
        List<MessageVo> message = chatServiceDelegator.receiveMessage(fromUserId, MessageType.TO_USER);
        return Result.success(message);
    }

    @Operation(summary = "分页查询消息")
    @GetMapping("/{userId}/{pageNum}/{pageSize}")
    public Result<Page<MessageVo>> findMessagePage(
            @PathVariable Integer pageNum,
            @PathVariable Integer pageSize,
            @PathVariable Long userId,
            Long beforeDateTime) {
        Page<MessageVo> page = chatServiceDelegator.findMessagePage(
                userId,
                new Date(beforeDateTime),
                pageNum,
                pageSize,
                MessageType.TO_USER
        );
        return Result.success(page);
    }
}
