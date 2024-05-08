package com.todo.util.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.entity.Message;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class JsonEncoder implements Encoder.Text<Message> {
    @Override
    public String encode(Message object) throws EncodeException {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new EncodeException(object, e.getMessage());
        }
    }
}