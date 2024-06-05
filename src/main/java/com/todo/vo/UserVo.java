package com.todo.vo;

import com.todo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {
    private Long userId;
    private String userName;
    private String email;
    private String avatar;
    private String signature;
    private Date createdAt;

    private Long tomatoDuration;

    public UserVo(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.avatar = user.getAvatar();
        this.signature = user.getSignature();
        this.createdAt = user.getCreatedAt();
    }
}
