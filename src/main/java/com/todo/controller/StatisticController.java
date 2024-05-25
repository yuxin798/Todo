package com.todo.controller;

import com.todo.service.impl.StatisticServiceImpl;
import com.todo.vo.Result;
import com.todo.vo.statistic.StatisticVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计接口
 */
@Tag(name = "数据统计API")
@RestController
@RequestMapping("/statistic")
public class StatisticController {

    private final StatisticServiceImpl statisticServiceImpl;

    public StatisticController(StatisticServiceImpl statisticServiceImpl) {
        this.statisticServiceImpl = statisticServiceImpl;
    }

    @Operation(summary = "获得统计数据")
    @GetMapping("/")
    public Result<StatisticVo> statistic() {
        StatisticVo statisticVo = statisticServiceImpl.statistic();
        return Result.success(statisticVo);
    }

    @Operation(summary = "每个任务的统计数据")
    @GetMapping("/{taskId}")
    public Result<StatisticVo> statisticByTask(@PathVariable Long taskId) {
        StatisticVo statisticVo = statisticServiceImpl.statisticByTask(taskId);
        return Result.success(statisticVo);
    }
}
