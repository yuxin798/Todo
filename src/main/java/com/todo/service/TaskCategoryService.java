package com.todo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.todo.dto.TaskCategoryDto;
import com.todo.entity.Message;
import com.todo.entity.TaskCategory;
import com.todo.vo.Result;
import com.todo.vo.TaskCategoryVo;
import com.todo.vo.TaskVo;

import java.util.List;

public interface TaskCategoryService extends IService<TaskCategory> {

    Result<TaskCategoryVo> addTaskCategory(TaskCategoryDto taskCategoryDto);

    Result<?> deleteTaskCategory(Long categoryId);

    Result<?> updateTaskCategory(TaskCategoryDto taskCategoryDto);

    Result<List<TaskCategoryVo>> getAll();

    Result<List<TaskVo>> getAllTasks(Long categoryId);
}
