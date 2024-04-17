package com.todo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.entity.Task;
import com.todo.mapper.TaskMapper;
import com.todo.service.TaskService;
import org.springframework.stereotype.Service;

/**
* @author 28080
* @description 针对表【task】的数据库操作Service实现
* @createDate 2024-04-17 17:09:33
*/
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
    implements TaskService {

}




