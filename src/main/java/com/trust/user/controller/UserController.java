package com.trust.user.controller;

import com.trust.user.entity.User;
import com.trust.user.service.impl.UserServiceImpl;
import com.trust.util.JwtUtil;
import com.trust.util.ResultVOUtil;
import com.trust.vo.Result;
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
