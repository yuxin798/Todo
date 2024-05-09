package com.todo.util.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.entity.Message;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;

public class JsonMessageDecoder implements Decoder.Text<Message> {
    @Override
    public Message decode(String s) throws DecodeException {
        try {
            return new ObjectMapper().readValue(s, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new DecodeException(s, e.getMessage());
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }
}
