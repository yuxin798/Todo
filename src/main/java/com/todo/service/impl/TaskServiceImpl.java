package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.dto.TaskDto;
import com.todo.entity.Task;
import com.todo.entity.TomatoClock;
import com.todo.entity.User;
import com.todo.mapper.TaskMapper;
import com.todo.mapper.TomatoClockMapper;
import com.todo.service.TaskService;
import com.todo.util.PageUtil;
import com.todo.util.UserContextUtil;
import com.todo.vo.TaskVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
* @author 28080
* @description 针对表【task】的数据库操作Service实现
* @createDate 2024-04-17 17:09:33
*/
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
    implements TaskService {

    private final TomatoClockMapper tomatoClockMapper;

    public TaskServiceImpl(TomatoClockMapper tomatoClockMapper) {
        this.tomatoClockMapper = tomatoClockMapper;
    }

    @Override
    public TaskVo addTask(TaskDto taskDto) {
        User user = UserContextUtil.getUser();
        String estimate = StringUtils.collectionToCommaDelimitedString(taskDto.getEstimate());

        Task task = new Task(user.getUserId(), taskDto.getTaskName(), estimate);
        baseMapper.insert(task);

        return findById(task.getTaskId());
    }

    @Override
    public void removeTask(Long taskId) {
        User user = UserContextUtil.getUser();
        Task task = baseMapper.selectById(taskId);

        if (task == null || !Objects.equals(task.getUserId(), user.getUserId())) {
            throw new RuntimeException("任务不存在");
        }

        // 删除task
        baseMapper.deleteById(task.getTaskId());

        // 删除tomato clock
        LambdaQueryWrapper<TomatoClock> wrapper = new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getTaskId, task.getTaskId());
        tomatoClockMapper.delete(wrapper);
    }

    @Override
    public TaskVo updateTask(TaskDto taskDto) {
        User user = UserContextUtil.getUser();
        Task task = baseMapper.selectById(taskDto.getTaskId());

        if (task == null || !Objects.equals(task.getUserId(), user.getUserId())) {
            throw new RuntimeException("任务不存在");
        }

        String estimate = StringUtils.collectionToCommaDelimitedString(taskDto.getEstimate());
        LambdaUpdateWrapper<Task> wrapper = new LambdaUpdateWrapper<>(Task.class)
                .set(StringUtils.hasText(taskDto.getTaskName()), Task::getTaskName, taskDto.getTaskName())
                .set(StringUtils.hasText(estimate), Task::getEstimate, estimate)
                .set(taskDto.getTomatoClockTimes() != null, Task::getTomatoClockTimes, taskDto.getTomatoClockTimes())
                .set(taskDto.getStopTimes() != null, Task::getStopTimes, taskDto.getStopTimes())
                .set(taskDto.getTaskStatus() != null, Task::getTaskStatus, taskDto.getTaskStatus())
                .set(taskDto.getInnerInterrupt() != null, Task::getInnerInterrupt, taskDto.getInnerInterrupt())
                .set(taskDto.getOuterInterrupt() != null, Task::getOuterInterrupt, taskDto.getOuterInterrupt())
                .set(taskDto.getStartedAt() != null, Task::getStartedAt, taskDto.getStartedAt())
                .set(taskDto.getCompletedAt() != null, Task::getCompletedAt, taskDto.getCompletedAt())
                .eq(Task::getTaskId, taskDto.getTaskId());

        baseMapper.update(task, wrapper);
        return findById(task.getTaskId());
    }

    @Override
    public Page<TaskVo> findTaskPage(TaskDto taskDto, int pageNum, int pageSize) {
        String estimate = StringUtils.collectionToCommaDelimitedString(taskDto.getEstimate());
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>(Task.class)
                .like(StringUtils.hasText(taskDto.getTaskName()), Task::getTaskName, taskDto.getTaskName())
                .eq(StringUtils.hasText(estimate), Task::getEstimate, estimate)
                .eq(taskDto.getTomatoClockTimes() != null, Task::getTomatoClockTimes, taskDto.getTomatoClockTimes())
                .eq(taskDto.getStopTimes() != null, Task::getStopTimes, taskDto.getStopTimes())
                .eq(taskDto.getTaskStatus() != null, Task::getTaskStatus, taskDto.getTaskStatus())
                .eq(taskDto.getInnerInterrupt() != null, Task::getInnerInterrupt, taskDto.getInnerInterrupt())
                .eq(taskDto.getOuterInterrupt() != null, Task::getOuterInterrupt, taskDto.getOuterInterrupt())
                .gt(taskDto.getStartedAt() != null, Task::getStartedAt, taskDto.getStartedAt())
                .lt(taskDto.getCompletedAt() != null, Task::getCompletedAt, taskDto.getCompletedAt());

        Page<Task> taskPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<TaskVo> list = taskPage.getRecords()
                .stream()
                .map(TaskVo::new)
                .peek(taskVo -> {
                    LambdaQueryWrapper<TomatoClock> w = new LambdaQueryWrapper<>(TomatoClock.class)
                            .eq(TomatoClock::getTaskId, taskVo.getTaskId());
                    List<TomatoClock> tomatoClocks = tomatoClockMapper.selectList(w);
                    taskVo.setTomatoClocks(tomatoClocks);
                }).toList();
        return PageUtil.of(taskPage, list);
    }

    @Override
    public TaskVo findById(Long taskId) {
        User user = UserContextUtil.getUser();
        Task task = baseMapper.selectById(taskId);

        if (task == null || !Objects.equals(task.getUserId(), user.getUserId())) {
            throw new RuntimeException("任务不存在");
        }

        LambdaQueryWrapper<TomatoClock> wrapper = new LambdaQueryWrapper<TomatoClock>(TomatoClock.class)
                .eq(TomatoClock::getTaskId, taskId);
        List<TomatoClock> tomatoClocks = tomatoClockMapper.selectList(wrapper);
        TaskVo taskVo = new TaskVo(task);
        taskVo.setTomatoClocks(tomatoClocks);
        return taskVo;
    }

    @Override
    public List<TaskVo> findAll() {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getUserId, UserContextUtil.getUser().getUserId());
        return baseMapper.selectList(wrapper)
                .stream()
                .map(TaskVo::new)
                .peek(taskVo -> {
                    LambdaQueryWrapper<TomatoClock> w = new LambdaQueryWrapper<TomatoClock>(TomatoClock.class)
                            .eq(TomatoClock::getTaskId, taskVo.getTaskId());
                    List<TomatoClock> tomatoClocks = tomatoClockMapper.selectList(w);
                    taskVo.setTomatoClocks(tomatoClocks);
                })
                .toList();
    }
}




