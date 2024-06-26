package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.constant.AmqpConstant;
import com.todo.entity.Message;
import com.todo.entity.User;
import com.todo.entity.UserRoom;
import com.todo.mapper.UserMapper;
import com.todo.mapper.UserRoomMapper;
import com.todo.util.UserContextUtil;
import com.todo.vo.MessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoomChatServiceImpl {
    private final AmqpAdmin amqpAdmin;
    private final AmqpTemplate amqpTemplate;
    private final RoomServiceImpl roomServiceImpl;
    private final RabbitTemplate rabbitTemplate;
    private final UserRoomMapper userRoomMapper;

    private final UserMapper userMapper;

    public RoomChatServiceImpl(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate, RoomServiceImpl roomServiceImpl, @Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate, UserRoomMapper userRoomMapper, UserMapper userMapper) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;
        this.roomServiceImpl = roomServiceImpl;
        this.rabbitTemplate = rabbitTemplate;
        this.userRoomMapper = userRoomMapper;
        this.userMapper = userMapper;
    }

    public void sendMessage(Message message) {
        Long userId = UserContextUtil.getUser().getUserId();
        Long roomId = message.getToRoomId();

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, userId)
                .eq(UserRoom::getRoomId, roomId);
        if (userRoomMapper.selectOne(wrapper) == null) {
            throw new RuntimeException("用户已不在自习室中");
        }

        QueueInformation queueInfo = amqpAdmin.getQueueInfo(AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + userId);

        if (queueInfo == null) {
            /*
                messages --> -- --> Room交换机 --> 用户队列1
                                               |
                                               --> 用户队列2
             */

            boolean exist = isExchangeExist(AmqpConstant.EXCHANGE_CHAT_ROOM + roomId);
            if (exist) {
                // 声明用户队列
                amqpAdmin.declareQueue(new Queue(AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + userId, true, false, false));

                amqpAdmin.declareBinding(new Binding(
                        AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + userId,
                        Binding.DestinationType.QUEUE,
                        AmqpConstant.EXCHANGE_CHAT_ROOM + roomId,
                        AmqpConstant.QUEUE_CHAT_ROOM + roomId,
                        null)
                );
            } else {
                // 声明Room的交换机
                amqpAdmin.declareExchange(new TopicExchange(AmqpConstant.EXCHANGE_CHAT_ROOM + roomId, true, false));

                roomServiceImpl.listUsers(roomId).forEach(userVo -> {
                    // 声明用户队列
                    amqpAdmin.declareQueue(new Queue(AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + userVo.getUserId(), true, false, false));

                    amqpAdmin.declareBinding(new Binding(
                            AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + userVo.getUserId(),
                            Binding.DestinationType.QUEUE,
                            AmqpConstant.EXCHANGE_CHAT_ROOM + roomId,
                            AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + userVo.getUserId(),
                            null)
                    );
                });
            }
        }

        amqpTemplate.convertAndSend(
                AmqpConstant.EXCHANGE_CHAT_ROOM + roomId,
                AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + message.getToUserId(),
                message
        );
        log.info("==> send message success. roomId: {}, message: {}", roomId, message);
    }


    public List<MessageVo> receiveMessage(Long roomId) {
        Long userId = UserContextUtil.getUser().getUserId();

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, userId)
                .eq(UserRoom::getRoomId, roomId);
        if (userRoomMapper.selectOne(wrapper) == null) {
            throw new RuntimeException("用户已不在自习室中");
        }

        QueueInformation queueInfo = amqpAdmin.getQueueInfo(AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + userId);
        if (queueInfo == null) return new ArrayList<>();

        ArrayList<Message> msgs = new ArrayList<>();
        Message message;

        while (null != (message = amqpTemplate.receiveAndConvert(
                AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + userId,
                ParameterizedTypeReference.forType(Message.class)))) {
            log.info("==> Received message. roomId: {}, message: {}", roomId, message);
            msgs.add(message);
        }

        List<Long> ids = userRoomMapper.selectList(
                new LambdaQueryWrapper<>(UserRoom.class)
                        .eq(UserRoom::getRoomId, roomId)
                )
                .stream()
                .map(UserRoom::getUserId)
                .toList();

        Map<Long, User> userMap = userMapper.selectBatchIds(ids)
                .stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        return msgs.stream()
                .map(MessageVo::new)
                .peek(messageVo -> {
                    messageVo.setFromUserName(userMap.get(messageVo.getFromUserId()).getUserName());
                    messageVo.setFromUserAvatar(userMap.get(messageVo.getFromUserId()).getAvatar());
                })
                .collect(Collectors.toList());
    }

    public boolean isExchangeExist(String exchangeName) {
        try {
            rabbitTemplate.execute(channel -> {
                channel.exchangeDeclarePassive(exchangeName);
                return null;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
