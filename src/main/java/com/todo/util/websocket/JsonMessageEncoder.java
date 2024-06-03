package com.todo.util.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.dto.MessageDto;
import com.todo.entity.Message;
import com.todo.vo.MessageVo;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class JsonMessageEncoder implements Encoder.Text<MessageVo> {
    @Override
    public String encode(MessageVo object) throws EncodeException {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new EncodeException(object, e.getMessage());
        }
    }
}