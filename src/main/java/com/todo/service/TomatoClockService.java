package com.todo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.todo.entity.TomatoClock;
import com.todo.vo.Result;
import com.todo.vo.TomatoClockVo;

import java.util.List;

/**
* @author 28080
* @description 针对表【tomato_clock】的数据库操作Service
* @createDate 2024-04-17 17:09:33
*/
public interface TomatoClockService extends IService<TomatoClock> {

    Result<List<TomatoClockVo>> addTomatoClock(Long taskId);

    Result<?> completeTomatoClock(Long clockId);

    Result<?> innerInterrupt(Long clockId);

    Result<?> outerInterrupt(Long clockId);

    Result<?> stopTomatoClock(Long taskId, String stopReason);

    Result<TomatoClockVo> findTomatoClock(Long clockId);

    Result<List<TomatoClockVo>> findTomatoClockAll(Long taskId);

    Result<?> deleteTomatoClock(Long taskId);

    Result<?> startTomatoClock(Long clockId);
}
