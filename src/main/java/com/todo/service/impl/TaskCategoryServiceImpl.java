package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.dto.TaskCategoryDto;
import com.todo.entity.Task;
import com.todo.entity.TaskCategory;
import com.todo.entity.TomatoClock;
import com.todo.mapper.TaskCategoryMapper;
import com.todo.mapper.TaskMapper;
import com.todo.mapper.TomatoClockMapper;
import com.todo.service.TaskCategoryService;
import com.todo.service.TaskService;
import com.todo.util.DateUtil;
import com.todo.util.UserContextUtil;
import com.todo.vo.Result;
import com.todo.vo.TaskCategoryVo;
import com.todo.vo.TaskVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskCategoryServiceImpl extends ServiceImpl<TaskCategoryMapper, TaskCategory>
        implements TaskCategoryService{

    private final TaskMapper taskMapper;
    private final TomatoClockMapper tomatoClockMapper;

    public TaskCategoryServiceImpl(TaskMapper taskMapper, TomatoClockMapper tomatoClockMapper) {
        this.taskMapper = taskMapper;
        this.tomatoClockMapper = tomatoClockMapper;
    }

    @Override
    public Result<TaskCategoryVo> addTaskCategory(TaskCategoryDto taskCategoryDto) {
        Long userId = UserContextUtil.getUserId();

        LambdaQueryWrapper<TaskCategory> queryWrapper = new LambdaQueryWrapper<>(TaskCategory.class)
                .eq(TaskCategory::getUserId, userId)
                .eq(TaskCategory::getCategoryName, taskCategoryDto.getCategoryName());
        if (this.getOne(queryWrapper) != null){
            throw new RuntimeException("已经添加过名称相同的清单集啦");
        }

        TaskCategory category = new TaskCategory(taskCategoryDto);
        category.setUserId(userId);
        this.save(category);

        TaskCategory taskCategory = this.getOne(queryWrapper);

        return Result.success(new TaskCategoryVo(taskCategory));
    }

    @Override
    public Result<?> deleteTaskCategory(Long categoryId) {
        Long userId = UserContextUtil.getUserId();
        LambdaQueryWrapper<TaskCategory> queryWrapper = new LambdaQueryWrapper<>(TaskCategory.class)
                .eq(TaskCategory::getUserId, userId)
                .eq(TaskCategory::getCategoryId, categoryId);
        TaskCategory taskCategory = this.getOne(queryWrapper);

        if (taskCategory == null){
            throw new RuntimeException("该分类不存在");
        }
        //删除清单表中清单分类
        this.remove(queryWrapper);
        LambdaQueryWrapper<Task> taskQueryMapper = new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getCategoryId, categoryId);
        List<Task> tasks = taskMapper.selectList(taskQueryMapper);
        //删除任务表中的该清单分类任务
        taskMapper.delete(taskQueryMapper);
        //删除番茄钟中的番茄钟
        List<Long> taskIds = tasks.stream()
                .map(Task::getTaskId)
                .distinct()
                .collect(Collectors.toList());
        if (!tasks.isEmpty()) {
            tomatoClockMapper.delete(new LambdaQueryWrapper<>(TomatoClock.class)
                    .in(TomatoClock::getTaskId, taskIds)
            );
        }
        return Result.success();
    }

    @Override
    public Result<TaskCategoryVo> updateTaskCategory(TaskCategoryDto taskCategoryDto) {
        LambdaUpdateWrapper<TaskCategory> updateWrapper = new LambdaUpdateWrapper<>(TaskCategory.class)
                .set(StringUtils.hasText(taskCategoryDto.getCategoryName()), TaskCategory::getCategoryName, taskCategoryDto.getCategoryName())
                .set(taskCategoryDto.getColor() != null, TaskCategory::getColor, taskCategoryDto.getColor())
                .eq(TaskCategory::getCategoryId, taskCategoryDto.getCategoryId());
        this.update(updateWrapper);
        TaskCategory taskCategory = this.getOne(new LambdaQueryWrapper<>(TaskCategory.class)
                .eq(TaskCategory::getCategoryId, taskCategoryDto.getCategoryId())
        );
        return Result.success(new TaskCategoryVo(taskCategory));
    }

    @Override
    public Result<List<TaskCategoryVo>> getAll() {
        LambdaQueryWrapper<TaskCategory> queryWrapper = new LambdaQueryWrapper<>(TaskCategory.class)
                .eq(TaskCategory::getUserId, UserContextUtil.getUserId())
                .orderByAsc(TaskCategory::getCreatedAt);
        List<TaskCategoryVo> taskCategoryVos = this.list(queryWrapper)
                .stream()
                .map(TaskCategoryVo::new)
                .toList();
        return Result.success(taskCategoryVos);
    }

    public Result<Map<TaskCategoryVo, List<TaskVo>>> getAllCategoryAndTasks() {
        LambdaQueryWrapper<TaskCategory> categoryQueryWrapper = new LambdaQueryWrapper<>(TaskCategory.class)
                .eq(TaskCategory::getUserId, UserContextUtil.getUserId());
        Map<Long, TaskCategoryVo> taskCategoryVoMap = this.list(categoryQueryWrapper)
                .stream()
                .map(TaskCategoryVo::new)
                .collect(Collectors.toMap(
                        TaskCategoryVo::getCategoryId, taskCategoryVo -> taskCategoryVo)
                );

        LambdaQueryWrapper<Task> taskQueryWrapper = new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getUserId, UserContextUtil.getUserId());
        Map<Long, List<TaskVo>> taskVos = taskMapper.selectList(taskQueryWrapper)
                .stream()
                .map(TaskVo::new)
                .collect(Collectors.groupingBy(TaskVo::getCategoryId));

        Map<TaskCategoryVo, List<TaskVo>> result = taskVos.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> taskCategoryVoMap.get(entry.getKey()), Map.Entry::getValue)
                );
        return Result.success(result);
    }

    @Override
    public Result<List<TaskVo>> getAllTasks(Long categoryId) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getCategoryId, categoryId)
                .eq(Task::getUserId, UserContextUtil.getUserId())
                .ge(Task::getCreatedAt, DateUtil.todayMinTime())
                .le(Task::getCreatedAt, DateUtil.todayMaxTime())
                .orderByAsc(Task::getCreatedAt);
        List<TaskVo> taskVos = taskMapper.selectList(queryWrapper)
                .stream()
                .map(TaskVo::new)
                .toList();
        return Result.success(taskVos);
    }
}
