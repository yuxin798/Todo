package com.todo.controller;

import com.todo.entity.User;
import com.todo.service.impl.UserServiceImpl;
import com.todo.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yuxin
 * @since 2024-04-16
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserServiceImpl userServiceImpl;

    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        return userServiceImpl.login(user);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        return userServiceImpl.register(user);
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
