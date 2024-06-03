package com.todo.service.impl;

import com.todo.constant.AmqpConstant;
import com.todo.entity.Message;
import com.todo.entity.User;
import com.todo.mapper.UserMapper;
import com.todo.util.UserContextUtil;
import com.todo.vo.MessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserChatServiceImpl {
    private final AmqpAdmin amqpAdmin;
    private final AmqpTemplate amqpTemplate;

    private final UserMapper userMapper;

    public UserChatServiceImpl(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate, UserMapper userMapper) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;
        this.userMapper = userMapper;
    }

    public void sendMessage(Message message) {
        Long fromUserId = UserContextUtil.getUser().getUserId();
        Long toUserId = message.getToUserId();

        String suffix = fromUserId + ":" + toUserId;
        QueueInformation queueInfo = amqpAdmin.getQueueInfo(AmqpConstant.QUEUE_CHAT_USER + suffix);
        if (queueInfo == null) {
            // 创建点对点的队列
            amqpAdmin.declareQueue(new Queue(AmqpConstant.QUEUE_CHAT_USER + suffix, true, false, false));

            amqpAdmin.declareExchange(new TopicExchange(AmqpConstant.EXCHANGE_CHAT_USER + suffix, true, false));

            amqpAdmin.declareBinding(new Binding(
                    AmqpConstant.QUEUE_CHAT_USER + suffix,
                    Binding.DestinationType.QUEUE,
                    AmqpConstant.EXCHANGE_CHAT_USER + suffix,
                    AmqpConstant.QUEUE_CHAT_USER + suffix,
                    null)
            );
        }

        amqpTemplate.convertAndSend(
                AmqpConstant.EXCHANGE_CHAT_USER + suffix,
                AmqpConstant.QUEUE_CHAT_USER + suffix,
                message
        );
        log.info("==> send message success. roomId: {}, message: {}", suffix, message);
    }

    public List<MessageVo> receiveMessage(Long fromUserId) {
        Long userId = UserContextUtil.getUser().getUserId();

        QueueInformation queueInfo = amqpAdmin.getQueueInfo(AmqpConstant.QUEUE_CHAT_USER + fromUserId + ":" + userId);
        if (queueInfo == null) return new ArrayList<>();

        ArrayList<Message> msgs = new ArrayList<>();
        Message message;

        while (null != (message = amqpTemplate.receiveAndConvert(
                AmqpConstant.QUEUE_CHAT_USER + fromUserId + ":" + userId,
                ParameterizedTypeReference.forType(Message.class)))) {
            log.info("==> Received message. fromUserId: {}, toUserId{}, message: {}", fromUserId, userId, message);
            msgs.add(message);
        }

        User user = userMapper.selectById(message.getFromUserId());

        return msgs.stream()
                .map(MessageVo::new)
                .peek(messageVo -> {
                    messageVo.setFromUserName(user.getUserName());
                    messageVo.setFromUserName(user.getAvatar());
                })
                .collect(Collectors.toList());
    }
}
