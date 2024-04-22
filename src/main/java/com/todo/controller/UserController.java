package com.todo.controller;

import com.todo.dto.UserDto;
import com.todo.entity.User;
import com.todo.service.impl.UserServiceImpl;
import com.todo.util.UserContextUtil;
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

    /**
     * 登录
     * @param user
     * @return token
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserDto user) {
        return userServiceImpl.login(user);
    }

    /**
     * 注册
     * @param user
     * @return 注册成功与否
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDto user) {
        return userServiceImpl.register(user);
    }

    @GetMapping("/test")
    public String test(){
        System.out.println(UserContextUtil.getUser());
        return "test";
    }
}
