package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.constant.RedisConstant;
import com.todo.dto.UserDto;
import com.todo.entity.User;
import com.todo.mapper.UserMapper;
import com.todo.service.UserService;
import com.todo.util.DefaultGeneratorUtils;
import com.todo.util.JwtUtil;
import com.todo.util.UserContextUtil;
import com.todo.vo.Result;
import com.todo.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, userDto.getEmail());
        User user = baseMapper.selectOne(queryWrapper);
        if(user == null || !passwordEncoder.matches(userDto.getPassword(), user.getPassword())){
            throw new RuntimeException("邮箱或密码错误");
        }
        String token = JwtUtil.sign(user);
        return Result.success(token);
    }

    @Override
    public Result<String> register(UserDto userDto) {
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            throw new RuntimeException("两次密码不一致");
        }

        //邮箱验证码验证
        String emailCode = redisTemplate.opsForValue().get(RedisConstant.USER_EMAIL_CODE + userDto.getEmailCodeKey() + userDto.getEmail());
        if (!StringUtils.hasText(emailCode) || !emailCode.equals(userDto.getEmailCode())){
            throw new RuntimeException("邮箱验证码错误");
        }

        User queryUser = baseMapper.selectOne(new LambdaQueryWrapper<>(User.class)
                .eq(User::getEmail, userDto.getEmail()));
        if (queryUser != null){
            throw new RuntimeException("帐号已存在，请更换邮箱");
        }

        //密码加密
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        User user = new User(userDto.getUserName(), userDto.getEmail(), userDto.getPassword(), DefaultGeneratorUtils.getRandomDefaultAvatar(), DefaultGeneratorUtils.getRandomDefaultSignature());
        if (this.save(user)){
            redisTemplate.expire(RedisConstant.USER_EMAIL_CODE + userDto.getEmailCodeKey() + userDto.getEmail(), 0, TimeUnit.SECONDS);
            return Result.success();
        }else {
            throw new RuntimeException("网络繁忙，请稍后重试");
        }
    }

    @Override
    public Result<String> updatePassword(UserDto userDto) {
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            throw  new RuntimeException("两次密码不一致");
        }
        if (!StringUtils.hasText(userDto.getEmail())) {
            //未传入email，校验token是否有效，从token中获取email
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if (StringUtils.hasText(token)){
                User user = JwtUtil.getUserByToken(token);
                if(JwtUtil.verify(token, user)){
                    userDto.setEmail(user.getEmail());
                }
            }
        }
        //校验email是否为null
        if(!StringUtils.hasText(userDto.getEmail())){
            throw new RuntimeException("请填写邮箱");
        }
        String emailCode = redisTemplate.opsForValue().get(RedisConstant.USER_EMAIL_CODE + userDto.getEmailCodeKey() + userDto.getEmail());
        if (!userDto.getEmailCode().equals(emailCode)){
            throw new RuntimeException("邮箱验证码错误");
        }
        redisTemplate.expire(RedisConstant.USER_EMAIL_CODE + userDto.getEmailCodeKey() + userDto.getEmail(), 0, TimeUnit.SECONDS);
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>(User.class)
                .set(User::getPassword, user.getPassword())
                .eq(User::getEmail, user.getEmail());
        baseMapper.update(updateWrapper);
        return Result.success();
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
            throw  new RuntimeException("请填写邮箱");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Todo");
        String code = UUID.randomUUID().toString().substring(0, 6);
        message.setText("验证码为：" + code);
        mailSender.send(message);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(RedisConstant.USER_EMAIL_CODE + uuid + email, code, 1, TimeUnit.MINUTES);
        return Result.success(uuid);
    }

    @Override
    public Result<String> updateSignature(String signature) {
        Long userId = UserContextUtil.getUser().getUserId();
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>(User.class)
                .set(User::getSignature, signature)
                .eq(User::getUserId, userId);
        int count = baseMapper.update(updateWrapper);
        if (count == 1){
            return Result.success();
        }
        throw new RuntimeException("用户已注销");
    }
    @Override
    public Result<String> updateUserName(String userName) {
        Long userId = UserContextUtil.getUser().getUserId();
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>(User.class)
                .set(User::getUserName, userName)
                .eq(User::getUserId, userId);
        int count = baseMapper.update(updateWrapper);
        if (count == 1){
            return Result.success();
        }
        throw new RuntimeException("用户已注销");
    }

    @Override
    public Result<String> modifyAvatar(String avatar) {
        Long userId = UserContextUtil.getUser().getUserId();
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>(User.class)
                .set(User::getAvatar, avatar)
                .eq(User::getUserId, userId);
        int count = baseMapper.update(updateWrapper);
        if (count == 1){
            return Result.success();
        }
        throw new RuntimeException("用户已注销");
    }

    @Override
    public Result<UserVo> modifyUserInfo(UserDto userDto) {
        Long userId = UserContextUtil.getUser().getUserId();
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>(User.class)
                .set(StringUtils.hasText(userDto.getUserName()), User::getUserName, userDto.getUserName())
                .set(StringUtils.hasText(userDto.getAvatar()), User::getAvatar, userDto.getAvatar())
                .set(StringUtils.hasText(userDto.getSignature()), User::getSignature, userDto.getSignature())
                .eq(User::getUserId, userId);
        this.update(updateWrapper);
        User user = this.getById(userId);
        return Result.success(new UserVo(user));
    }

    @Override
    public Result<UserVo> getUserInfo() {
        Long userId = UserContextUtil.getUser().getUserId();
        User user = baseMapper.selectById(userId);
        if (user == null){
            throw new RuntimeException("用户已注销");
        }
        UserVo userVo = new UserVo(user);
        userVo.setEmail(user.getEmail());
        return Result.success(userVo);
    }

    @Override
    public Result<String> logout() {
        Long userId = UserContextUtil.getUser().getUserId();
        int count = baseMapper.deleteById(userId);
        if (count == 1){
            return Result.success();
        }
        throw new RuntimeException("用户已注销");
    }

    @Override
    public Result<UserVo> getUserInfo(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
        return Result.success(new UserVo(user));
    }
}




