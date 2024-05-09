package com.todo.util.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.dto.MessageDto;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;

public class JsonMessageDtoDecoder implements Decoder.Text<MessageDto> {
    @Override
    public MessageDto decode(String s) throws DecodeException {
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
