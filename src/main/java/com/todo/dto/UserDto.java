package com.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String userName;
    private String email;
    private String password;
    private String confirmPassword;
    private String emailCodeKey;
    private String emailCode;
}
