package com.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.User;
import org.apache.ibatis.annotations.Param;

/**
* @author 28080
* @description 针对表【user】的数据库操作Mapper
* @createDate 2024-04-17 17:09:33
* @Entity com.todo.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

    User findUserByEmail(String email);

    boolean updateUserByUserId(@Param("userId") Long userId, @Param("userName")String userName, @Param("password")String password);
}




