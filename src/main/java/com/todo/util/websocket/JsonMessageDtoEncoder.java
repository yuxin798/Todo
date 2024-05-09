package com.todo.util.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.dto.MessageDto;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;

public class JsonMessageDtoEncoder implements Encoder.Text<MessageDto> {
    @Override
    public String encode(MessageDto object) throws EncodeException {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new EncodeException(object, e.getMessage());
        }
    }
}