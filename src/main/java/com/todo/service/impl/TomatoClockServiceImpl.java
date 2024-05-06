package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.todo.entity.TomatoClock;
import com.todo.mapper.TomatoClockMapper;
import com.todo.service.TomatoClockService;
import com.todo.vo.Result;
import jakarta.validation.constraints.NotNull;
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

    @Override
    public Result<?> addTomatoClock(List<TomatoClock> tomatoClockList) {
        // 为第一个番茄钟添加 初始值
        TomatoClock first = tomatoClockList.stream().findFirst().get();
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
        LambdaUpdateWrapper<TomatoClock> updateWrapper = new LambdaUpdateWrapper<>(TomatoClock.class)
                .set(TomatoClock::getClockStatus, 0)
                .set(TomatoClock::getCompletedAt, new Date())
                .eq(TomatoClock::getClockId, clockId);
        if (update(updateWrapper)) {
            return Result.success("修改成功");
        }
        return Result.error("网络异常，请稍后重试");
    }

    @Override
    public Result<?> innerInterrupt(Long clockId, Integer innerInterrupt) {
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
        LambdaUpdateWrapper<TomatoClock> updateWrapper = new LambdaUpdateWrapper<>(TomatoClock.class)
                .set(TomatoClock::getOuterInterrupt, 1 + outerInterrupt)
                .eq(TomatoClock::getClockId, clockId);
        if (update(updateWrapper)) {
            return Result.success("修改成功");
        }
        return Result.error("网络异常，请稍后重试");
    }
}




