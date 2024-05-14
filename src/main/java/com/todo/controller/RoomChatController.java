package com.todo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.todo.entity.Message;
import com.todo.service.impl.ChatServiceImplDelegator;
import com.todo.service.impl.ChatServiceImplDelegator.MessageType;
import com.todo.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@Slf4j
@Tag(name = "自习室聊天API")
@RestController
@RequestMapping("/chat/room")
public class RoomChatController {
    private final ChatServiceImplDelegator chatServiceDelegator;

    public RoomChatController(ChatServiceImplDelegator chatServiceDelegator) {
        this.chatServiceDelegator = chatServiceDelegator;
    }

    @Operation(summary = "接收消息")
    @GetMapping("/receive")
    public Result<List<Message>> receiveMessage(Long roomId) {
        List<Message> message = chatServiceDelegator.receiveMessage(roomId, MessageType.TO_ROOM);
        return Result.success(message);
    }

    @Operation(summary = "分页查询消息")
    @GetMapping("/{roomId}/{pageNum}/{pageSize}")
    public Result<Page<Message>> findMessagePage(
            @PathVariable Integer pageNum,
            @PathVariable Integer pageSize,
            @PathVariable Long roomId,
            Long beforeDateTime) {
        Page<Message> page = chatServiceDelegator.findMessagePage(
                roomId,
                new Date(beforeDateTime),
                pageNum,
                pageSize,
                MessageType.TO_ROOM
        );
        return Result.success(page);
    }
}
