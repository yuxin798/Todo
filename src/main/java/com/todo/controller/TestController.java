package com.todo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;


@RestController
@Tag(name = "Test")
public class TestController {
    @GetMapping("/test")
    public String test() {
        return "SUCCESS!";
    }
    @Autowired
    private StringRedisTemplate redisTemplate;


    @Operation(summary = "RedisTest")
    @GetMapping("/test/redis")
    public String testRedis() {
        redisTemplate.opsForValue().set("redisTest", "SUCCESS!", 5, TimeUnit.SECONDS);
        return redisTemplate.opsForValue().get("redisTest");
    }
}
