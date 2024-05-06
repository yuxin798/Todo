package com.todo.controller;

import com.todo.constant.MinioConstant;
import com.todo.dto.UserDto;
import com.todo.fileupload.FileUploadService;
import com.todo.service.impl.UserServiceImpl;
import com.todo.vo.Result;
import com.todo.vo.UserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

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
    @Autowired
    private FileUploadService fileUploadService;

    /*
     *获取验证码Key
     */
    @Operation(summary = "获取验证码Key")
    @GetMapping("/getEmailCodeKey")
    public Result<String> getEmailCodeKey(String email){
        return userServiceImpl.sendEmail(email);
    }

    /*
     * 注册
     */
    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Validated({UserDto.Register.class}) UserDto userDto) {
        return userServiceImpl.register(userDto);
    }

    /*
     * 登录
     */
    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated({UserDto.Login.class}) UserDto userDto) {
        return userServiceImpl.login(userDto);
    }


    /*
     *修改密码/忘记密码
     */
    @Operation(summary = "修改密码/忘记密码")
    @PutMapping("/modifyPassword")
    public Result<String> modifyPassword(@RequestBody @Validated({UserDto.ModifyPassword.class}) UserDto userDto){
        return userServiceImpl.updatePassword(userDto);
    }

    /*
     * 上传头像
     */
    @Operation(summary = "上传头像")
    @PostMapping( "/uploadAvatar")
    public Result<String> uploadAvatar(MultipartFile avatar){
        if (avatar == null || avatar.isEmpty()) {
            return Result.error("头像不能为空");
        }
        String avatarUrl = fileUploadService.save(avatar, MinioConstant.USER_ROOT_PATH);
        return Result.success(avatarUrl);
    }

    /*
     * 修改头像
     */
    @Operation(summary = "修改头像")
    @PutMapping("/modifyAvatar")
    public Result<String> modifyAvatar(@NotBlank(message = "头像不能为空") @RequestParam String avatar){
        return userServiceImpl.modifyAvatar(avatar);
    }

    /*
     * 修改用户名
     */
    @Operation(summary = "修改用户名")
    @PutMapping("/modifyUserName")
    public Result<String> modifyUserName(@NotBlank(message = "用户名不能为空") @RequestParam String userName){
        return userServiceImpl.updateUserName(userName);
    }

    /*
     * 修改个性签名
     */
    @Operation(summary = "修改个性签名")
    @PutMapping("/modifySignature")
    public Result<String> modifySignature(@NotBlank(message = "个性签名不能为空") @RequestParam String signature){
        return userServiceImpl.updateSignature(signature);
    }

    /*
     * 获取用户信息
     */
    @Operation(summary = "获取用户信息")
    @GetMapping("/getUserInfo")
    public Result<UserVo> getUserInfo(){
        return userServiceImpl.getUserInfo();
    }

    /*
     * 用户注销
     */
    @Operation(summary = "用户注销")
    @GetMapping("/logout")
    public Result<String> logout(){
        return userServiceImpl.logout();
    }
}
