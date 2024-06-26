package com.todo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.todo.entity.User;
import com.todo.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    UserMapper userMapper;
    @Test
    void testSelectOne() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", "1");
        User user = userMapper.selectOne(queryWrapper);
    }
}
