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
            return Result.error("邮箱或密码错误");
        }
        String token = JwtUtil.sign(user);
        return Result.success("认证成功", token);
    }

    @Override
    public Result<String> register(UserDto userDto) {
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            return Result.error("两次密码不一致");
        }
        User queryUser = baseMapper.findUserByEmail(userDto.getEmail());
        //用户已注册且未注销
        if (queryUser != null && queryUser.getDeleted() == 0){
            return Result.error("帐号已存在，请更换邮箱");
        }
        //邮箱验证码验证
        String emailCode = redisTemplate.opsForValue().get(RedisConstant.EMAIL_VALIDATE_CODE + userDto.getEmailCodeKey() + userDto.getEmail());
        if (!StringUtils.hasText(emailCode) || !emailCode.equals(userDto.getEmailCode())){
            return Result.error("邮箱验证码错误");
        }
        //密码加密
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        //用户已注册且已注销，重新注册
        if(queryUser != null && queryUser.getDeleted() == 1){
            if (baseMapper.updateUserByUserId(queryUser.getUserId(), userDto.getUserName(), userDto.getPassword())){
                redisTemplate.expire(RedisConstant.EMAIL_VALIDATE_CODE + userDto.getEmailCodeKey() + userDto.getEmail(), 0, TimeUnit.SECONDS);
                return Result.success("注册成功");
            }else {
                return Result.error("网络繁忙，请稍后重试");
            }
        }
        //用户从未注册过
        User user = new User(userDto.getUserName(), userDto.getEmail(), userDto.getPassword(), DefaultGeneratorUtils.getRandomDefaultAvatar(), DefaultGeneratorUtils.getRandomDefaultSignature());
        if (this.save(user)){
            redisTemplate.expire(RedisConstant.EMAIL_VALIDATE_CODE + userDto.getEmailCodeKey() + userDto.getEmail(), 0, TimeUnit.SECONDS);
            return Result.success("注册成功");
        }else {
            return Result.error("网络繁忙，请稍后重试");
        }
    }

    @Override
    public Result<String> updatePassword(UserDto userDto) {
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            return Result.error("两次密码不一致");
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
            return Result.error("请填写邮箱");
        }
        String emailCode = redisTemplate.opsForValue().get(RedisConstant.EMAIL_VALIDATE_CODE + userDto.getEmailCodeKey() + userDto.getEmail());
        if (!userDto.getEmailCode().equals(emailCode)){
            return Result.error("邮箱验证码错误");
        }
        redisTemplate.expire(RedisConstant.EMAIL_VALIDATE_CODE + userDto.getEmailCodeKey() + userDto.getEmail(), 0, TimeUnit.SECONDS);
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        int count = baseMapper.updatePasswordByEmail(user);
        if (count == 1){
            return Result.success("密码修改成功");
        }else {
            return Result.error("网络繁忙，请稍后重试");
        }
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
        redisTemplate.opsForValue().set(RedisConstant.EMAIL_VALIDATE_CODE + uuid + email, code, 1, TimeUnit.MINUTES);
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
            return Result.success("修改成功");
        }
        return Result.error("用户已注销");
    }
    @Override
    public Result<String> updateUserName(String userName) {
        Long userId = UserContextUtil.getUser().getUserId();
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>(User.class)
                .set(User::getUserName, userName)
                .eq(User::getUserId, userId);
        int count = baseMapper.update(updateWrapper);
        if (count == 1){
            return Result.success("修改成功");
        }
        return Result.error("用户已注销");
    }

    @Override
    public Result<String> modifyAvatar(String avatar) {
        Long userId = UserContextUtil.getUser().getUserId();
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>(User.class)
                .set(User::getAvatar, avatar)
                .eq(User::getUserId, userId);
        int count = baseMapper.update(updateWrapper);
        if (count == 1){
            return Result.success("修改成功");
        }
        return Result.error("用户已注销");
    }

    public Result<UserVo> getUserInfo() {
        Long userId = UserContextUtil.getUser().getUserId();
        User user = baseMapper.selectById(userId);
        if (user == null){
            return Result.error("用户已注销");
        }
        UserVo userVo = new UserVo(user.getUserId(), user.getUserName(), user.getAvatar(), user.getSignature());
        return Result.success(userVo);
    }

    @Override
    public Result<String> logout() {
        Long userId = UserContextUtil.getUser().getUserId();
        int count = baseMapper.deleteById(userId);
        if (count == 1){
            return Result.success("注销成功");
        }
        return Result.success("用户已注销");
    }
}




