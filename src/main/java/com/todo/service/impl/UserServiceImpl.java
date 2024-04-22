package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.dto.UserDto;
import com.todo.entity.User;
import com.todo.mapper.UserMapper;
import com.todo.service.UserService;
import com.todo.util.JwtUtil;
import com.todo.util.ResultVOUtil;
import com.todo.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
* @author 28080
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-04-17 17:09:33
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    PasswordEncoder passwordEncoder;

    public Result<String> login(UserDto userDto) {
        if (userDto == null){
            return ResultVOUtil.error("请填写用户名和密码");
        }else if(userDto.getUserName() == null){
            return ResultVOUtil.error("请填写用户名");
        }else if(userDto.getPassword() == null){
            return ResultVOUtil.error("请填写密码");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userDto.getUserName());
        User queryUser = userMapper.selectOne(queryWrapper);
        if(queryUser == null || !passwordEncoder.matches(userDto.getPassword(), queryUser.getPassword())){
            return ResultVOUtil.error("用户名或密码错误");
        }
        String token = JwtUtil.sign(queryUser);
        return ResultVOUtil.success("认证成功", token);
    }

    public Result<String> register(User user) {
        if (user == null){
            return ResultVOUtil.error("请填写用户名和密码");
        }else if(user.getUserName() == null){
            return ResultVOUtil.error("请填写用户名");
        }else if(user.getPassword() == null){
            return ResultVOUtil.error("请填写密码");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", user.getUserName());
        boolean exists = userMapper.exists(queryWrapper);
        if (exists){
            return ResultVOUtil.error("帐号已存在，请更换用户名");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        boolean save = this.save(user);
        if (save){
            return ResultVOUtil.success("注册成功");
        }else {
            return ResultVOUtil.error("网络繁忙，请稍后重试");
        }
    }
}




