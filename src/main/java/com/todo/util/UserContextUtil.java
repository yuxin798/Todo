package com.todo.util;

import com.todo.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContextUtil {
    public static User getUser(){
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
