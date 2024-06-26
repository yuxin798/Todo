package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.entity.Task;
import com.todo.entity.TomatoClock;
import com.todo.mapper.TaskMapper;
import com.todo.mapper.TomatoClockMapper;
import com.todo.service.TomatoClockService;
import com.todo.util.UserContextUtil;
import com.todo.vo.Result;
import com.todo.vo.TomatoClockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.todo.entity.TomatoClock.Status.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author 28080
 * @description 针对表【tomato_clock】的数据库操作Service实现
 * @createDate 2024-04-17 17:09:33
 */
@Service
public class TomatoClockServiceImpl extends ServiceImpl<TomatoClockMapper, TomatoClock>
        implements TomatoClockService {

    private final TaskMapper taskMapper;

    @Autowired
    public TomatoClockServiceImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public Result<List<TomatoClockVo>> addTomatoClock(Long taskId) {
        // 查询是否存在该任务，以及该任务是否属于自己
        Task task = dataVerificationAndAuthenticationByTaskId(taskId);

        Integer clockDuration = task.getClockDuration();
        String[] split = task.getEstimate().split(",");
        int estimate = Integer.parseInt(split[split.length - 1]);

        List<TomatoClock> tomatoClocks = new ArrayList<>();
        IntStream.rangeClosed(1, estimate).forEach(i -> {
            TomatoClock tomatoClock = new TomatoClock(taskId, clockDuration, task.getParentId(), task.getUserId());
            if (i == 1) {
                Date startedAt = new Date();
                // 为第一个番茄钟添加 初始值
                tomatoClock.setStartedAt(startedAt);
                tomatoClock.clockStatusEnum(DOING);
                taskMapper.update(new LambdaUpdateWrapper<>(Task.class)
                        .set(Task::getStartedAt, startedAt)
                        .eq(Task::getTaskId, taskId)
                );
            }
            this.save(tomatoClock);
            tomatoClocks.add(tomatoClock);
        });

        // 返回新增的番茄钟列表
        return Result.success(tomatoClocks.stream().map(TomatoClockVo::new).collect(Collectors.toList()));
    }

    @Override
    public Result<?> startTomatoClock(Long clockId) {
        TomatoClock tomatoClock = dataVerificationAndAuthenticationByClockId(clockId);
        TomatoClock.Status clockStatus = tomatoClock.clockStatusEnum();

        // 断言番茄钟的状态为未开始
        assertStatus(clockStatus, NOT_STARTED);

        LambdaUpdateWrapper<TomatoClock> updateWrapper = new LambdaUpdateWrapper<>(TomatoClock.class)
                .set(TomatoClock::getClockStatus, DOING.getCode())
                .set(TomatoClock::getStartedAt, new Date())
                .eq(TomatoClock::getClockId, clockId)
                .eq(TomatoClock::getClockStatus, NOT_STARTED.getCode());
        this.update(updateWrapper);
        return Result.success();
    }

    @Override
    public Result<Map<Long, List<TomatoClockVo>>> statisticHistoryByTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);

        if (task == null || !Objects.equals(task.getUserId(), UserContextUtil.getUserId())){
            throw new RuntimeException("任务不存在");
        }

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getParentId, task.getParentId());
        List<Task> tasks = taskMapper.selectList(wrapper);

        Map<Long, String> taskIdName = new HashMap<>();
        tasks.forEach(t -> taskIdName.put(t.getTaskId(), t.getTaskName()));

        LambdaQueryWrapper<TomatoClock> queryWrapper = new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getParentId, task.getParentId())
                .in(TomatoClock::getClockStatus, COMPLETED.getCode(), TERMINATED.getCode());
        Map<Long, List<TomatoClockVo>> statistic = this.list(queryWrapper)
                .stream()
                .map(t -> new TomatoClockVo(t, taskIdName.get(t.getTaskId())))
                .collect(Collectors.groupingBy(
                        t -> 1000 * LocalDate.ofInstant(t.getCompletedAt().toInstant(), ZoneId.of("UTC+8")).atStartOfDay().toEpochSecond(ZoneOffset.of("+8"))
                ));
        return Result.success(statistic);
    }

    @Override
    public Result<Map<Long, List<TomatoClockVo>>> statisticHistoryByUser() {
        LambdaQueryWrapper<Task> taskQueryWrapper = new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getUserId, UserContextUtil.getUserId())
                .in(Task::getTaskStatus, Task.Status.COMPLETED.getCode());
        List<Task> tasks = taskMapper.selectList(taskQueryWrapper);

        Map<Long, String> taskIdName = new HashMap<>();
        tasks.forEach(t -> taskIdName.put(t.getTaskId(), t.getTaskName()));

        List<Long> parentIds = tasks
                .stream()
                .map(Task::getParentId)
                .distinct()
                .toList();

        if (CollectionUtils.isEmpty(parentIds)){
            return Result.success(new HashMap<>());
        }

        LambdaQueryWrapper<TomatoClock> queryWrapper = new LambdaQueryWrapper<>(TomatoClock.class)
                .in(TomatoClock::getParentId, parentIds)
                .in(TomatoClock::getClockStatus, COMPLETED.getCode(), TERMINATED.getCode());
        Map<Long, List<TomatoClockVo>> statistic = this.list(queryWrapper)
                .stream()
                .map(t -> new TomatoClockVo(t, taskIdName.get(t.getTaskId())))
                .collect(Collectors.groupingBy(
                        t -> 1000 * LocalDate.ofInstant(t.getCompletedAt().toInstant(), ZoneId.of("UTC+8")).atStartOfDay().toEpochSecond(ZoneOffset.of("+8"))
                ));
        return Result.success(statistic);
    }

    @Override
    public Result<?> advanceCompleteTask(Long taskId) {
        dataVerificationAndAuthenticationByTaskId(taskId);

        List<TomatoClock> tomatoClocks = this.list(new LambdaUpdateWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getTaskId, taskId)
        );

        boolean doing = tomatoClocks
                .stream()
                .anyMatch(t -> Objects.equals(t.getClockStatus(), DOING.getCode()));

        if (doing){
            List<TomatoClock> doingClocks = tomatoClocks.stream()
                    .filter(t -> Objects.equals(t.getClockStatus(), DOING.getCode()))
                    .toList();

            Date startedAt = doingClocks.get(doingClocks.size() - 1).getStartedAt();
            Date completedAt = new Date();
            LocalDateTime startTime = startedAt.toInstant().atZone(ZoneId.of("+8")).toLocalDateTime();
            LocalDateTime endTime = completedAt.toInstant().atZone(ZoneId.of("+8")).toLocalDateTime();
            Duration duration = Duration.between(startTime, endTime);

            LambdaUpdateWrapper<TomatoClock> updateWrapper = new LambdaUpdateWrapper<>(TomatoClock.class)
                    .set(TomatoClock::getClockStatus, COMPLETED.getCode())
                    .set(TomatoClock::getCompletedAt, completedAt)
                    .set(TomatoClock::getClockDuration, duration.toMinutes())
                    .eq(TomatoClock::getTaskId, taskId)
                    .eq(TomatoClock::getClockStatus, DOING.getCode());
            this.update(updateWrapper);
        }

        this.remove(new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getClockStatus, NOT_STARTED.getCode())
                .eq(TomatoClock::getTaskId, taskId)
        );
        return Result.success();
    }

    /**
     * 断言番茄中的状态
     * @param clockStatus    番茄钟的状态
     * @param expectedStatus 预期番茄钟的状态
     */
    private void assertStatus(TomatoClock.Status clockStatus, TomatoClock.Status expectedStatus) {
        if (clockStatus == expectedStatus) return;

        if (clockStatus == COMPLETED) {
            throw new RuntimeException("该番茄钟已完成");
        } else if (clockStatus == DOING) {
            throw new RuntimeException("该番茄钟已开始");
        } else if (clockStatus == NOT_STARTED) {
            throw new RuntimeException("该番茄钟未开始");
        } else if (clockStatus == TERMINATED) {
            throw new RuntimeException("该番茄钟已停止");
        }
    }

    @Override
    public Result<?> completeTomatoClock(Long clockId) {
        TomatoClock tomatoClock = dataVerificationAndAuthenticationByClockId(clockId);
        TomatoClock.Status clockStatus = tomatoClock.clockStatusEnum();

        // 断言番茄钟的状态为已开始
        assertStatus(clockStatus, DOING);

        LambdaUpdateWrapper<TomatoClock> updateWrapper = new LambdaUpdateWrapper<>(TomatoClock.class)
                .set(TomatoClock::getClockStatus, COMPLETED.getCode())
                .set(TomatoClock::getCompletedAt, new Date())
                .eq(TomatoClock::getClockId, clockId)
                .eq(TomatoClock::getClockStatus, DOING.getCode());
        update(updateWrapper);

        return Result.success();
    }

    @Override
    public Result<?> stopTomatoClock(Long taskId, String stopReason) {
        dataVerificationAndAuthenticationByTaskId(taskId);

        LambdaUpdateWrapper<TomatoClock> updateWrapper = new LambdaUpdateWrapper<>(TomatoClock.class)
                .set(TomatoClock::getClockStatus, TERMINATED.getCode())
                .set(stopReason != null, TomatoClock::getStopReason, stopReason)
                .set(TomatoClock::getCompletedAt, new Date())
                .eq(TomatoClock::getTaskId, taskId)
                .eq(TomatoClock::getClockStatus, DOING.getCode());
        this.update(updateWrapper);

        this.remove(new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getClockStatus, NOT_STARTED.getCode())
                .eq(TomatoClock::getTaskId, taskId)
        );
        return Result.success();
    }

    @Override
    public Result<TomatoClockVo> findTomatoClock(Long clockId) {
        TomatoClock tomatoClock = dataVerificationAndAuthenticationByClockId(clockId);
        return Result.success(new TomatoClockVo(tomatoClock));
    }

    @Override
    public Result<List<TomatoClockVo>> findTomatoClockAll(Long taskId) {
        dataVerificationAndAuthenticationByTaskId(taskId);
        return Result.success(
                this.list(new LambdaQueryWrapper<TomatoClock>()
                                .eq(TomatoClock::getTaskId, taskId))
                        .stream()
                        .map(TomatoClockVo::new)
                        .toList()
        );
    }

    @Override
    public Result<?> deleteTomatoClock(Long taskId) {
        dataVerificationAndAuthenticationByTaskId(taskId);

        this.remove(new LambdaQueryWrapper<TomatoClock>()
                .eq(TomatoClock::getTaskId, taskId));
        return Result.success();
    }

    // 根据番茄钟Id进行数据校验及身份认证
    private TomatoClock dataVerificationAndAuthenticationByClockId(Long clockId) {
        TomatoClock tomatoClock = this.getById(clockId);
        if (tomatoClock == null || !tomatoClock.getUserId().equals(UserContextUtil.getUserId())) {
            throw new RuntimeException("不存在该番茄钟");
        }

        Task task = taskMapper.selectById(tomatoClock.getTaskId());
        if (task == null) {
            throw new RuntimeException("不存在该任务");
        }

        return tomatoClock;
    }

    // 根据任务Id进行数据校验及身份认证
    private Task dataVerificationAndAuthenticationByTaskId(Long taskId) {
        Task task = taskMapper.selectById(taskId);

        if (task == null || !task.getUserId().equals(UserContextUtil.getUserId())) {
            throw new RuntimeException("不存在该任务");
        }

        return task;
    }
}




