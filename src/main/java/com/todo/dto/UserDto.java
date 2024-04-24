package com.todo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @NotBlank(message = "用户名不能为空", groups = {Register.class})
    private String userName;
    @NotBlank(message = "邮箱不能为空", groups = {Register.class, Login.class})
    @Email(message = "邮箱格式错误",groups = {Register.class, Login.class})
    private String email;
    @NotBlank(message = "密码不能为空", groups = {Register.class, Login.class, ModifyPassword.class})
    @Length(min = 6,max = 16, message = "密码长度必须在6-16范围内", groups = {Register.class, ModifyPassword.class})
    private String password;
    @NotBlank(message = "确认密码不能为空", groups = {Register.class, ModifyPassword.class})
    private String confirmPassword;
    @NotBlank(message = "验证码Key不能为空", groups = {Register.class, ModifyPassword.class})
    private String emailCodeKey;
    @NotBlank(message = "验证码不能为空", groups = {Register.class, ModifyPassword.class})
    private String emailCode;

    public interface Login{
    }

    public interface Register{

    }

    public interface ModifyPassword {

    }
}
