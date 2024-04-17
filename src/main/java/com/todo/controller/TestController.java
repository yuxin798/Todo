package com.todo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.TimeUnit;

@Controller
public class TestController {
    @GetMapping("/test")
    public String test() {
        return "SUCCESS!";
    }
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/test/redis")
    public String testRedis() {
        redisTemplate.opsForValue().set("redisTest", "SUCCESS!", 5, TimeUnit.SECONDS);
        return redisTemplate.opsForValue().get("redisTest");
    }
}
