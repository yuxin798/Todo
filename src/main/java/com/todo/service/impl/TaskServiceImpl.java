package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.dto.TaskDto;
import com.todo.entity.*;
import com.todo.mapper.TaskMapper;
import com.todo.mapper.TomatoClockMapper;
import com.todo.service.TaskService;
import com.todo.util.DateUtil;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.todo.entity.Task.Status.*;

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
        String estimate = "1";
        if (!CollectionUtils.isEmpty(taskDto.getEstimate())){
            estimate = StringUtils.collectionToCommaDelimitedString(taskDto.getEstimate());
        }
        
        Task task = new Task(UserContextUtil.getUserId(), taskDto.getTaskName(), taskDto.getType(), estimate, taskDto.getClockDuration(), DefaultGeneratorUtils.getRandomDefaultBackground(), taskDto.getRemark(), taskDto.getRestTime(), taskDto.getAgain());

        if (taskDto.getType() == 0 && taskDto.getClockDuration() == null){
            task.setClockDuration(25);
        }

        // 不存在分类 存放到 待办列表
        if (taskDto.getCategoryId() == null){
            task.taskStatusEnum(TODO_TODAY);
        }else {
            // 存在分类 存放到 清单列表
            task.taskStatusEnum(CHECKLIST);
            task.setCategoryId(taskDto.getCategoryId());
        }

        //插入今天的任务
        task.setParentId(DefaultGeneratorUtils.nextId());
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(Date.from(now.atZone(ZoneId.of("+8")).toInstant()));
        baseMapper.insert(task);
        Long taskId = task.getTaskId();

        //如果循环 同时插入明天的任务
        if (taskDto.getAgain() == 0){
            task.setCreatedAt(Date.from(now.plusDays(1).atZone(ZoneId.of("+8")).toInstant()));
            task.setTaskId(null);
            baseMapper.insert(task);
        }
        return findById(taskId);
    }

    @Override
    public void removeTask(Long taskId) {
        Task task = baseMapper.selectById(taskId);

        if (task == null || !Objects.equals(task.getUserId(), UserContextUtil.getUserId())) {
            throw new RuntimeException("任务不存在");
        }

        LambdaUpdateWrapper<Task> updateWrapper = new LambdaUpdateWrapper<>(Task.class)
                .eq(Task::getParentId, task.getParentId())
                .ge(Task::getCreatedAt, DateUtil.todayMinTime());
        baseMapper.delete(updateWrapper);

        // 删除tomato clock
        LambdaQueryWrapper<TomatoClock> wrapper = new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getParentId, task.getParentId())
                .ge(TomatoClock::getCreatedAt, DateUtil.todayMinTime());
        tomatoClockMapper.delete(wrapper);
    }

    @Override
    public TaskVo updateTask(TaskDto taskDto) {
        Task task = baseMapper.selectById(taskDto.getTaskId());

        if (task == null || !Objects.equals(task.getUserId(), UserContextUtil.getUserId())) {
            throw new RuntimeException("任务不存在");
        }

        // 将 again 改为 1 需要删除明天的任务数据  
        // 将 again 改为 0 需要添加明天的任务数据
        if (taskDto.getAgain() != null && taskDto.getAgain() == 1 && task.getAgain() == 0){
            baseMapper.delete(new LambdaUpdateWrapper<>(Task.class)
                    .eq(Task::getParentId, task.getParentId())
                    .ge(Task::getCreatedAt, DateUtil.tomorrowMinTime())
            );
        }else if (taskDto.getAgain() != null && taskDto.getAgain() == 0 && task.getAgain() == 1){
            Date createdAt = task.getCreatedAt();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.of("+8"));
            Date tomorrowDate = Date.from(localDateTime.plusDays(1).toInstant(ZoneOffset.of("+8")));
            task.setCreatedAt(tomorrowDate);
            task.setTaskId(null);
            baseMapper.insert(task);
        }

        String estimate = StringUtils.collectionToCommaDelimitedString(taskDto.getEstimate());
        LambdaUpdateWrapper<Task> wrapper = new LambdaUpdateWrapper<>(Task.class)
                .set(StringUtils.hasText(taskDto.getTaskName()), Task::getTaskName, taskDto.getTaskName())
                .set(taskDto.getType() != null, Task::getType, taskDto.getType())
                .set(StringUtils.hasText(estimate), Task::getEstimate, estimate)
                .set(taskDto.getClockDuration() != null, Task::getClockDuration, taskDto.getClockDuration())
                .set(taskDto.getRestTime() != null, Task::getRestTime, taskDto.getRestTime())
                .set(taskDto.getAgain() != null, Task::getAgain, taskDto.getAgain())
                .set(StringUtils.hasText(taskDto.getRemark()), Task::getRemark, taskDto.getRemark())
                .eq(Task::getParentId, task.getParentId())
                .ge(Task::getCreatedAt, DateUtil.todayMinTime());
        baseMapper.update(wrapper);

        return findById(taskDto.getTaskId());
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
                .ge(taskDto.getStartedAt() != null, Task::getStartedAt, taskDto.getStartedAt())
                .le(taskDto.getCompletedAt() != null, Task::getCompletedAt, taskDto.getCompletedAt())
                .eq(Task::getUserId, UserContextUtil.getUser().getUserId());

        Page<Task> taskPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<TaskVo> list = taskPage.getRecords()
                .stream()
                .map(TaskVo::new)
//                .peek(taskVo -> {
//                    LambdaQueryWrapper<TomatoClock> w = new LambdaQueryWrapper<>(TomatoClock.class)
//                            .eq(TomatoClock::getTaskId, taskVo.getTaskId());
//                    List<TomatoClockVo> tomatoClockVoList = tomatoClockMapper
//                            .selectList(w)
//                            .stream()
//                            .map(TomatoClockVo::new)
//                            .toList();
//                    taskVo.setTomatoClocks(tomatoClockVoList);
//                })
                .toList();
        return PageUtil.of(taskPage, list);
    }

    @Override
    public TaskVo findById(Long taskId) {
        Task task = baseMapper.selectById(taskId);

        if (task == null || !Objects.equals(task.getUserId(), UserContextUtil.getUserId())) {
            throw new RuntimeException("任务不存在");
        }

        LambdaQueryWrapper<TomatoClock> wrapper = new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getParentId, task.getParentId())
                .ge(TomatoClock::getCreatedAt, DateUtil.todayMinTime());
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
                            .eq(TomatoClock::getParentId, taskVo.getParentId());
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

        if (task.getType() == 2){
            Date now = new Date();
            this.update(new LambdaUpdateWrapper<>(Task.class)
                            .set(Task::getTodayTotalTimes, task.getTodayTotalTimes() + 1)
                            .set(Task::getStartedAt, now)
                            .set(Task::getCompletedAt, now)
                            .eq(Task::getTaskId, taskId)
            );
            return Result.success();
        }

        LocalDate date = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC+8"));
        Date start = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"))));
        Date end = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MAX, ZoneOffset.of("+8"))));

        LambdaQueryWrapper<TomatoClock> queryWrapper = new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getParentId, task.getParentId())
                .ge(TomatoClock::getStartedAt, task.getStartedAt())
                .le(TomatoClock::getCompletedAt, end)
                .orderByAsc(TomatoClock::getStartedAt);
        List<TomatoClock> tomatoClockList = tomatoClockMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(tomatoClockList)) {
            throw new RuntimeException("该任务不可能已完成");
        }

        TomatoClock lastTomatoClock;

        Optional<TomatoClock> reduce = tomatoClockList
                .stream()
                .filter(tomatoClock ->
                        tomatoClock.getCompletedAt() != null
                        && (tomatoClock.clockStatusEnum() == TomatoClock.Status.COMPLETED
                        || tomatoClock.clockStatusEnum() == TomatoClock.Status.TERMINATED))
                .reduce((first, second) -> second);

        if (reduce.isPresent()){
            lastTomatoClock = reduce.get();
        }else {
            throw new RuntimeException("该任务不可能已完成");
        }

        Date completedAt = lastTomatoClock.getCompletedAt();

        if (task.getType() == 1){
            Date startedAt = task.getStartedAt();
            LocalDateTime startTime = startedAt.toInstant().atZone(ZoneId.of("+8")).toLocalDateTime();
            LocalDateTime endTime = completedAt.toInstant().atZone(ZoneId.of("+8")).toLocalDateTime();
            // 计算两个LocalDateTime对象之间的时间差
            Duration duration = Duration.between(startTime, endTime);
            tomatoClockMapper.update(new LambdaUpdateWrapper<>(TomatoClock.class)
                    .set(TomatoClock::getClockDuration, duration.toMinutes())
                    .eq(TomatoClock::getClockId, lastTomatoClock.getClockId())
            );
        }

        AtomicInteger innerInterrupt = new AtomicInteger(0);
        AtomicInteger outerInterrupt = new AtomicInteger(0);
        AtomicInteger tomatoClockTimes = new AtomicInteger(0);
        AtomicInteger stopTimes = new AtomicInteger(0);
        tomatoClockList
                .forEach(tomatoClock -> {
                    if (tomatoClock.clockStatusEnum() == TomatoClock.Status.COMPLETED) {
                        tomatoClockTimes.incrementAndGet();
                    } else if (tomatoClock.clockStatusEnum() == TomatoClock.Status.TERMINATED) {
                        stopTimes.incrementAndGet();
                    }
                    innerInterrupt.addAndGet(tomatoClock.getInnerInterrupt());
                    outerInterrupt.addAndGet(tomatoClock.getOuterInterrupt());
                });

        Long completedTimes = tomatoClockMapper.selectCount(new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getParentId, task.getParentId())
                .eq(TomatoClock::getClockStatus, TomatoClock.Status.COMPLETED.getCode())
                .ge(TomatoClock::getStartedAt, start)
                .le(TomatoClock::getCompletedAt, end)
        );

        this.update(new LambdaUpdateWrapper<>(Task.class)
                .set(Task::getCompletedAt, completedAt)
                .set(Task::getInnerInterrupt, innerInterrupt.intValue())
                .set(Task::getOuterInterrupt, outerInterrupt.intValue())
                .set(Task::getTomatoClockTimes, tomatoClockTimes.intValue())
                .set(Task::getStopTimes, stopTimes.intValue())
                .set(Task::getTaskStatus, COMPLETED.getCode())
                .set(Task::getTodayTotalTimes, completedTimes)
                .eq(Task::getTaskId, taskId));
        return Result.success();
    }

    @Override
    public Result<List<TaskVo>> findByDay(Long timestamp) {
        LocalDate date = LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC+8"));
        Date start = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"))));
        Date end = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MAX, ZoneOffset.of("+8"))));

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getUserId, UserContextUtil.getUserId())
                .in(Task::getTaskStatus, TODO_TODAY.getCode(), COMPLETED.getCode())
                .ge(Task::getCreatedAt, start)
                .le(Task::getCreatedAt, end)
                .orderByAsc(Task::getTaskStatus);
        return Result.success(
                baseMapper.selectList(wrapper)
                        .stream()
                        .map(TaskVo::new)
//                        .peek(taskVo -> {
//                            LambdaQueryWrapper<TomatoClock> w = new LambdaQueryWrapper<>(TomatoClock.class)
//                                    .eq(TomatoClock::getTaskId, taskVo.getTaskId());
//                            List<TomatoClockVo> tomatoClockVoList = tomatoClockMapper
//                                    .selectList(w)
//                                    .stream()
//                                    .map(TomatoClockVo::new)
//                                    .toList();
//                            taskVo.setTomatoClocks(tomatoClockVoList);
//                        })
                        .toList()
        );
    }
}




