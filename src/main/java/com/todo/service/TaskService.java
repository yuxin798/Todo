package com.todo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.todo.dto.TaskDto;
import com.todo.entity.Task;
import com.todo.vo.TaskVo;

import java.util.List;

/**
* @author 28080
* @description 针对表【task】的数据库操作Service
* @createDate 2024-04-17 17:09:33
*/
public interface TaskService extends IService<Task> {

    TaskVo addTask(TaskDto taskDto);

    void removeTask(Long taskDto);

    TaskVo updateTask(TaskDto taskDto);

    Page<TaskVo> findTaskPage(TaskDto taskDto, int pageNum, int pageSize);

    TaskVo findById(Long taskId);

    List<TaskVo> findAll();
}
