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
import com.todo.util.DefaultGeneratorUtils;
import com.todo.util.PageUtil;
import com.todo.util.UserContextUtil;
import com.todo.vo.Result;
import com.todo.vo.TaskVo;
import com.todo.vo.TomatoClockVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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

        Task task = new Task(user.getUserId(), taskDto.getTaskName(), estimate, taskDto.getClockDuration(), DefaultGeneratorUtils.getRandomDefaultBackground(), taskDto.getRemark(), taskDto.getRestTime(), taskDto.getAgain());

        // 不存在分类 存放到 待办列表
        if (!StringUtils.hasText(taskDto.getCategory())){
            task.setTaskStatus(0);
        }else {
            // 存在分类 存放到 清单列表
            task.setTaskStatus(1);
            task.setCategory(taskDto.getCategory());
        }

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
                .set(taskDto.getClockDuration() != null, Task::getClockDuration, taskDto.getClockDuration())
                .set(StringUtils.hasText(taskDto.getCategory()), Task::getCategory, taskDto.getCategory())
                .set(taskDto.getRestTime() != null, Task::getRestTime, taskDto.getRestTime())
                .set(taskDto.getAgain() != null, Task::getAgain, taskDto.getAgain())
                .set(StringUtils.hasText(taskDto.getRemark()), Task::getRemark, taskDto.getRemark())
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
                .lt(taskDto.getCompletedAt() != null, Task::getCompletedAt, taskDto.getCompletedAt())
                .eq(Task::getUserId, UserContextUtil.getUser().getUserId());

        Page<Task> taskPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<TaskVo> list = taskPage.getRecords()
                .stream()
                .map(TaskVo::new)
                .peek(taskVo -> {
                    LambdaQueryWrapper<TomatoClock> w = new LambdaQueryWrapper<>(TomatoClock.class)
                            .eq(TomatoClock::getTaskId, taskVo.getTaskId());
                    List<TomatoClockVo> tomatoClockVoList = tomatoClockMapper
                            .selectList(w)
                            .stream()
                            .map(TomatoClockVo::new)
                            .toList();
                    taskVo.setTomatoClocks(tomatoClockVoList);
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

        LambdaQueryWrapper<TomatoClock> wrapper = new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getTaskId, taskId);
        List<TomatoClockVo> tomatoClockVoList = tomatoClockMapper
                .selectList(wrapper)
                .stream()
                .map(TomatoClockVo::new)
                .toList();
        TaskVo taskVo = new TaskVo(task);
        taskVo.setTomatoClocks(tomatoClockVoList);
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
                    LambdaQueryWrapper<TomatoClock> w = new LambdaQueryWrapper<>(TomatoClock.class)
                            .eq(TomatoClock::getTaskId, taskVo.getTaskId());
                    List<TomatoClockVo> tomatoClockVoList = tomatoClockMapper
                            .selectList(w)
                            .stream()
                            .map(TomatoClockVo::new)
                            .toList();
                    taskVo.setTomatoClocks(tomatoClockVoList);
                })
                .toList();
    }

    @Override
    public Result<?> complete(Long taskId) {
        Task task = this.getById(taskId);
        if (task == null) {
            throw new RuntimeException("不存在该任务");
        }
        if (!task.getUserId().equals(UserContextUtil.getUser().getUserId())) {
            throw new RuntimeException("没有权限");
        }

        LambdaQueryWrapper<TomatoClock> queryWrapper = new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getTaskId, taskId);
        List<TomatoClock> tomatoClockList = tomatoClockMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(tomatoClockList)) {
            throw new RuntimeException("该任务不可能已完成");
        }

        Date startedAt = tomatoClockList.get(0).getStartedAt();
        Date completedAt;

        Optional<TomatoClock> reduce = tomatoClockList
                .stream()
                .filter(tomatoClock -> tomatoClock.getCompletedAt() != null && (tomatoClock.getClockStatus() == 0 || tomatoClock.getClockStatus() == 3))
                .reduce((first, second) -> second);

        if (reduce.isPresent()){
            completedAt = reduce.get().getCompletedAt();
        }else {
            throw new RuntimeException("该任务不可能已完成");
        }

        AtomicInteger innerInterrupt = new AtomicInteger(0);
        AtomicInteger outerInterrupt = new AtomicInteger(0);
        AtomicInteger tomatoClockTimes = new AtomicInteger(0);
        AtomicInteger stopTimes = new AtomicInteger(0);
        tomatoClockList
                .forEach(tomatoClock -> {
                    if (tomatoClock.getClockStatus() == 0) {
                        tomatoClockTimes.incrementAndGet();
                    } else if (tomatoClock.getClockStatus() == 3) {
                        stopTimes.incrementAndGet();
                    }
                    innerInterrupt.addAndGet(tomatoClock.getInnerInterrupt());
                    outerInterrupt.addAndGet(tomatoClock.getOuterInterrupt());
                });

        this.update(new LambdaUpdateWrapper<>(Task.class)
                .set(Task::getStartedAt, startedAt)
                .set(Task::getCompletedAt, completedAt)
                .set(Task::getInnerInterrupt, innerInterrupt.intValue())
                .set(Task::getOuterInterrupt, outerInterrupt.intValue())
                .set(Task::getTomatoClockTimes, tomatoClockTimes.intValue())
                .set(Task::getStopTimes, stopTimes.intValue())
                .set(Task::getTaskStatus, 2)
                .eq(Task::getTaskId, taskId));
        return Result.success();
    }

    @Override
    public Result<List<TaskVo>> findByCategory(String category) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getUserId, UserContextUtil.getUser().getUserId())
                .in(Task::getTaskStatus, 1, 2)
                .eq(Task::getCategory, category);
        return Result.success(
                baseMapper.selectList(wrapper)
                        .stream()
                        .map(TaskVo::new)
                        .peek(taskVo -> {
                            LambdaQueryWrapper<TomatoClock> w = new LambdaQueryWrapper<>(TomatoClock.class)
                                    .eq(TomatoClock::getTaskId, taskVo.getTaskId());
                            List<TomatoClockVo> tomatoClockVoList = tomatoClockMapper
                                    .selectList(w)
                                    .stream()
                                    .map(TomatoClockVo::new)
                                    .toList();
                            taskVo.setTomatoClocks(tomatoClockVoList);
                        })
                        .toList()
        );
    }

    @Override
    public Result<List<TaskVo>> findByDay(Long timestamp) {
        LocalDate date = LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC+8"));
        Date start = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"))));
        Date end = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MAX, ZoneOffset.of("+8"))));

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getUserId, UserContextUtil.getUser().getUserId())
                .in(Task::getTaskStatus, 0, 2)
                .ge(Task::getCreatedAt, start)
                .le(Task::getCreatedAt, end)
                .orderByAsc(Task::getTaskStatus);
        return Result.success(
                baseMapper.selectList(wrapper)
                        .stream()
                        .map(TaskVo::new)
                        .peek(taskVo -> {
                            LambdaQueryWrapper<TomatoClock> w = new LambdaQueryWrapper<>(TomatoClock.class)
                                    .eq(TomatoClock::getTaskId, taskVo.getTaskId());
                            List<TomatoClockVo> tomatoClockVoList = tomatoClockMapper
                                    .selectList(w)
                                    .stream()
                                    .map(TomatoClockVo::new)
                                    .toList();
                            taskVo.setTomatoClocks(tomatoClockVoList);
                        })
                        .toList()
        );
    }
}




