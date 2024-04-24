package com.todo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.todo.dto.UserDto;
import com.todo.entity.User;
import com.todo.vo.Result;

/**
* @author 28080
* @description 针对表【user】的数据库操作Service
* @createDate 2024-04-17 17:09:33
*/
public interface UserService extends IService<User> {
    Result<String> register(UserDto user);

    Result<String> login(UserDto userDto);

    Result<String> updatePassword(UserDto userDto);

    Result<String> sendEmail(String email);

    Result<String> updateSignature(String signature);

    Result<String> updateUserName(String userName);

    Result<String> modifyAvatar(String avatar);
}
