package com.todo.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.todo.entity.User;
import com.todo.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.ArrayList;

public class DBUserDetailsManager implements UserDetailsManager, UserDetailsPasswordService {
    @Autowired
    private UserServiceImpl UserServiceImpl;
    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        return null;
    }

    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", username);
        User user = UserServiceImpl.getOne(queryWrapper);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        } else {
            return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(), true, true, true, true, new ArrayList<>());
        }
    }
}
