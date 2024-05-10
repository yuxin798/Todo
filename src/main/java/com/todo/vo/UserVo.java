package com.todo.vo;

import com.todo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {
    private Long userId;
    private String userName;
    private String avatar;
    private String signature;

    public UserVo(Long userId, String userName, String avatar) {
        this.userId = userId;
        this.userName = userName;
        this.avatar = avatar;
    }

    public UserVo(User user) {
        this.userName = user.getUserName();
        this.avatar = user.getAvatar();
        this.signature = user.getSignature();
    }
}
