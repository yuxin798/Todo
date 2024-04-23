package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.constant.RedisConstant;
import com.todo.dto.UserDto;
import com.todo.entity.User;
import com.todo.mapper.UserMapper;
import com.todo.service.UserService;
import com.todo.util.DefaultImageUtils;
import com.todo.util.JwtUtil;
import com.todo.util.UserContextUtil;
import com.todo.vo.Result;
import io.minio.credentials.Jwt;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
* @author 28080
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-04-17 17:09:33
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Value("${spring.mail.username}")
    private String from;
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public Result<String> login(UserDto userDto) {
        if (userDto == null){
            return Result.error("请填写用户名和密码");
        }else if(userDto.getUserName() == null){
            return Result.error("请填写用户名");
        }else if(userDto.getPassword() == null){
            return Result.error("请填写密码");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userDto.getUserName());
        User queryUser = baseMapper.selectOne(queryWrapper);
        if(queryUser == null || !passwordEncoder.matches(userDto.getPassword(), queryUser.getPassword())){
            return Result.error("用户名或密码错误");
        }
        String token = JwtUtil.sign(queryUser);
        return Result.success("认证成功", token);
    }

    @Override
    public Result<String> register(UserDto user) {
        if (user == null){
            return Result.error("请填写用户名和密码");
        }else if(StringUtils.hasText(user.getUserName())){
            return Result.error("请填写用户名");
        }else if(StringUtils.hasText(user.getPassword())){
            return Result.error("请填写密码");
        }else if(StringUtils.hasText(user.getConfirmPassword())){
            return Result.error("请填写确认密码");
        }else if (!user.getPassword().equals(user.getConfirmPassword())){
            return Result.error("两次密码不一致");
        }else if(!StringUtils.hasText(user.getEmail())){
            return Result.error("请填写邮箱");
        }else if(!StringUtils.hasText(user.getEmailCodeKey())){
            return Result.error("请填写邮箱验证码");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, user.getUserName());
        if (baseMapper.exists(queryWrapper)){
            return Result.error("帐号已存在，请更换用户名");
        }
        String emailCode = redisTemplate.opsForValue().get(RedisConstant.EMAIL_VALIDATE_CODE + user.getEmailCodeKey());
        if (!StringUtils.hasText(emailCode) || !emailCode.equals(user.getEmailCode())){
            return Result.error("邮箱验证码错误");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        boolean save = this.save(new User(user.getUserName(), user.getPassword(), DefaultImageUtils.getRandomDefaultAvatar()));
        if (save){
            return Result.success("注册成功");
        }else {
            return Result.error("网络繁忙，请稍后重试");
        }
    }

    @Override
    public Result<String> updatePassword(UserDto userDto) {
        if (!StringUtils.hasText(userDto.getPassword())){
            return Result.error("请填写密码");
        }else if (!StringUtils.hasText(userDto.getConfirmPassword())){
            return Result.error("请填写确认密码");
        }else if(userDto.getPassword().equals(userDto.getConfirmPassword())){
            return Result.error("密码和确认密码不一致");
        }else if(!StringUtils.hasText(userDto.getEmailCodeKey())){
            return Result.error("请填写邮箱验证码");
        }else if(!redisTemplate.opsForValue().get(RedisConstant.EMAIL_VALIDATE_CODE + userDto.getEmailCodeKey()).equals(userDto.getEmailCode())){
            return Result.error("邮箱验证码错误");
        }
        User user = UserContextUtil.getUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        baseMapper.updateById(user);
        return Result.success("密码修改成功");
    }

    @Override
    public Result<String> sendEmail(String email) {
        if (!StringUtils.hasText(email)) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if (StringUtils.hasText(token)){
                User user = JwtUtil.getUserByToken(token);
                // 这边拿到的 用户名 应该去数据库查询获得密码，简略，步骤在service直接获取密码
                if(JwtUtil.verify(token, user)){
                    email = user.getEmail();
                }
            }
        }
        if(!StringUtils.hasText(email)){
            return Result.error("请填写邮箱");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Todo");
        String code = UUID.randomUUID().toString().substring(0, 6);
        message.setText("验证码为：" + code);
        mailSender.send(message);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(RedisConstant.EMAIL_VALIDATE_CODE + uuid, code, 1, TimeUnit.MINUTES);
        System.out.println(code);
        return Result.success(uuid);
    }
}




