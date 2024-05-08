package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.constant.AmqpConstant;
import com.todo.entity.Message;
import com.todo.entity.UserRoom;
import com.todo.mapper.UserRoomMapper;
import com.todo.service.RoomChatService;
import com.todo.util.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RoomChatServiceImpl implements RoomChatService {
    private final AmqpAdmin amqpAdmin;
    private final AmqpTemplate amqpTemplate;
    private final RoomServiceImpl roomServiceImpl;
    private final RabbitTemplate rabbitTemplate;
    private final UserRoomMapper userRoomMapper;

    public RoomChatServiceImpl(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate, RoomServiceImpl roomServiceImpl, @Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate, UserRoomMapper userRoomMapper) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;
        this.roomServiceImpl = roomServiceImpl;
        this.rabbitTemplate = rabbitTemplate;
        this.userRoomMapper = userRoomMapper;
    }

    @Override
    public void sendMessage(Message message) {
        Long userId = UserContextUtil.getUser().getUserId();
        Long roomId = message.getToRoomId();

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
                            AmqpConstant.QUEUE_CHAT_ROOM + roomId,
                            null)
                    );
                });
            }
        }

        amqpTemplate.convertAndSend(
                AmqpConstant.EXCHANGE_CHAT_ROOM + roomId,
                AmqpConstant.QUEUE_CHAT_ROOM + roomId,
                message
        );
        log.info("==> send message success. roomId: {}, message: {}", roomId, message);
    }

    @Override
    public List<Message> receiveMessage(Long roomId) {
        Long userId = UserContextUtil.getUser().getUserId();

        QueueInformation queueInfo = amqpAdmin.getQueueInfo(AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + userId);
        if (queueInfo == null) throw new RuntimeException("自习室不存在");

        LambdaQueryWrapper<UserRoom> wrapper = new LambdaQueryWrapper<>(UserRoom.class)
                .eq(UserRoom::getUserId, userId)
                .eq(UserRoom::getRoomId, roomId);
        if (userRoomMapper.selectOne(wrapper) == null) {
            throw new RuntimeException("用户已不在自习室中");
        }

        ArrayList<Message> msgs = new ArrayList<>();
        Message message;

        while (null != (message = amqpTemplate.receiveAndConvert(
                AmqpConstant.QUEUE_CHAT_ROOM + roomId + ":" + userId,
                ParameterizedTypeReference.forType(Message.class)))) {
            log.info("==> Received message. roomId: {}, message: {}", roomId, message);
            msgs.add(message);
        }

        return msgs;
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
