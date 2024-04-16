package com.trust.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.trust.user.entity.User;
import com.trust.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trust.util.JwtUtil;
import com.trust.util.ResultVOUtil;
import com.trust.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yuxin
 * @since 2024-04-16
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IService<User> {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    PasswordEncoder passwordEncoder;

    public Result<String> login(User user) {
        if (user == null){
            return ResultVOUtil.error("请填写用户名和密码");
        }else if(user.getUserName() == null){
            return ResultVOUtil.error("请填写用户名");
        }else if(user.getPassword() == null){
            return ResultVOUtil.error("请填写密码");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", user.getUserName());
        User queryUser = userMapper.selectOne(queryWrapper);
        if(queryUser == null || !passwordEncoder.matches(user.getPassword(), queryUser.getPassword())){
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
