package com.todo.controller;

import com.todo.dto.UserDto;
import com.todo.service.impl.UserServiceImpl;
import com.todo.util.UserContextUtil;
import com.todo.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "用户API")
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
    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserDto user) {
        return userServiceImpl.login(user);
    }

    /**
     * 注册
     * @param user
     * @return 注册成功与否
     */
    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDto user) {
        return userServiceImpl.register(user);
    }

    @Operation(summary = "获取验证码Key")
    @GetMapping("/getEmailCodeKey")
    public Result<String> getEmailCodeKey(String email){
        return userServiceImpl.sendEmail(email);
    }

    @Operation(summary = "修改密码/忘记密码")
    @PostMapping("/modifyPassword")
    public Result<String> modifyPassword(@RequestBody UserDto userDto){
        return userServiceImpl.updatePassword(userDto);
    }
}
