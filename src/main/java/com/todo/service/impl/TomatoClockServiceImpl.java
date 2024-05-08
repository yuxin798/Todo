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

import java.util.Date;
import java.util.List;

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
    public Result<?> addTomatoClock(List<TomatoClock> tomatoClockList) {
        // 查询是否存在该任务，以及该任务是否属于自己
        TomatoClock first = tomatoClockList.stream().findFirst().get();
        DataVerificationAndAuthenticationByTaskId(first.getTaskId());

        // 为第一个番茄钟添加 初始值
        first.setClockStatus(1);
        first.setStartedAt(new Date());

        // 获取当前任务已经完成的番茄钟数量
        Long taskId = first.getTaskId();
        LambdaQueryWrapper<TomatoClock> queryWrapper = new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getTaskId, taskId);
        int count = (int) count(queryWrapper);

        // 存储番茄钟
        tomatoClockList
                .stream()
                .peek(tomatoClock -> tomatoClock.setSequence(tomatoClock.getSequence() + count))
                .forEach(this::save);
        return Result.success("添加成功");
    }

    @Override
    public Result<?> completeTomatoClock(Long clockId) {
        TomatoClock tomatoClock = DataVerificationAndAuthenticationByClockId(clockId);

        if (tomatoClock.getClockStatus() == 0){
            return Result.error("该番茄钟已完成");
        }

        LambdaUpdateWrapper<TomatoClock> updateWrapper = new LambdaUpdateWrapper<>(TomatoClock.class)
                .set(TomatoClock::getClockStatus, 0)
                .set(TomatoClock::getCompletedAt, new Date())
                .eq(TomatoClock::getClockId, clockId)
                .eq(TomatoClock::getClockStatus, 1);
        if (update(updateWrapper)) {
            return Result.success("修改成功");
        }
        return Result.error("网络异常，请稍后重试");
    }

    @Override
    public Result<?> innerInterrupt(Long clockId, Integer innerInterrupt) {
        DataVerificationAndAuthenticationByClockId(clockId);

        LambdaUpdateWrapper<TomatoClock> updateWrapper = new LambdaUpdateWrapper<>(TomatoClock.class)
                .set(TomatoClock::getInnerInterrupt, 1 + innerInterrupt)
                .eq(TomatoClock::getClockId, clockId);
        if (update(updateWrapper)) {
            return Result.success("修改成功");
        }
        return Result.error("网络异常，请稍后重试");
    }

    @Override
    public Result<?> outerInterrupt(Long clockId, Integer outerInterrupt) {
        DataVerificationAndAuthenticationByClockId(clockId);

        LambdaUpdateWrapper<TomatoClock> updateWrapper = new LambdaUpdateWrapper<>(TomatoClock.class)
                .set(TomatoClock::getOuterInterrupt, 1 + outerInterrupt)
                .eq(TomatoClock::getClockId, clockId);
        if (update(updateWrapper)) {
            return Result.success("修改成功");
        }
        return Result.error("网络异常，请稍后重试");
    }

    @Override
    public Result<?> stopTomatoClock(Long taskId, String stopReason) {
        DataVerificationAndAuthenticationByTaskId(taskId);

        LambdaUpdateWrapper<TomatoClock> updateWrapper = new LambdaUpdateWrapper<>(TomatoClock.class)
                .set(TomatoClock::getClockStatus, 3)
                .set(TomatoClock::getStopReason, stopReason)
                .eq(TomatoClock::getTaskId, taskId)
                .and(wrapper -> wrapper
                        .eq(TomatoClock::getClockStatus, 1)
                        .or()
                        .eq(TomatoClock::getClockStatus, 2));

        if (update(updateWrapper)) {
            return Result.success("修改成功");
        }
        return Result.error("网络异常，请稍后重试");
    }

    @Override
    public Result<TomatoClockVo> findTomatoClock(Long clockId) {
        TomatoClock tomatoClock = DataVerificationAndAuthenticationByClockId(clockId);
        return Result.success(new TomatoClockVo(tomatoClock));
    }

    @Override
    public Result<List<TomatoClockVo>> findTomatoClockAll(Long taskId) {
        DataVerificationAndAuthenticationByTaskId(taskId);
        return Result.success(
                this.list(new LambdaQueryWrapper<TomatoClock>()
                        .eq(TomatoClock::getTaskId, taskId)
                        .orderByAsc(TomatoClock::getSequence))
                        .stream()
                        .map(TomatoClockVo::new)
                        .toList()
        );
    }

    //根据番茄钟Id进行数据校验及身份认证
    private TomatoClock DataVerificationAndAuthenticationByClockId(Long clockId){
        TomatoClock tomatoClock = this.getById(clockId);
        if (tomatoClock == null){
            throw new RuntimeException("不存在该番茄钟");
        }
        Task task = taskMapper.selectById(tomatoClock.getTaskId());
        if (task == null){
            throw new RuntimeException("不存在该任务");
        }
        if (!task.getUserId().equals(UserContextUtil.getUser().getUserId())){
            throw new RuntimeException("没有权限");
        }
        return tomatoClock;
    }

    //根据任务Id进行数据校验及身份认证
    private void DataVerificationAndAuthenticationByTaskId(Long taskId){
        Task task = taskMapper.selectById(taskId);
        if (task == null){
            throw new RuntimeException("不存在该任务");
        }
        if (!task.getUserId().equals(UserContextUtil.getUser().getUserId())){
            throw new RuntimeException("没有权限");
        }
    }
}




