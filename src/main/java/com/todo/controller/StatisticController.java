package com.todo.controller;

import com.google.common.util.concurrent.AtomicLongMap;
import com.todo.entity.TomatoClock;
import com.todo.service.impl.TaskServiceImpl;
import com.todo.util.LocalDateTimeUtil;
import com.todo.vo.Result;
import com.todo.vo.TaskVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 数据统计接口
 */
@Tag(name = "数据统计API")
@RestController
@RequestMapping("/statistic")
public class StatisticController {

    private final TaskServiceImpl taskServiceImpl;

    public StatisticController(TaskServiceImpl taskServiceImpl) {
        this.taskServiceImpl = taskServiceImpl;
    }

    @Operation(summary = "获得统计数据")
    @GetMapping("/")
    public Result<Map<String, Object>> getData() {
        HashMap<String, Object> map = new HashMap<>();

        List<TaskVo> tasks = taskServiceImpl.findAll();

        // 专注总时间次数
        AtomicLong tomatoTimes = new AtomicLong(0);
        // 专注总时间
        AtomicLong tomatoDuration = new AtomicLong(0);
        // 某天专注次数
        // 某天专注时长
        TreeMap<Long, List<TomatoClock>> dayTomato;

        dayTomato = tasks.stream()
                .parallel()
                .flatMap(t -> t.getTomatoClocks().stream())
                .filter(t -> t.getTaskStatus() == 0)
                .peek(t -> {
                    tomatoDuration.addAndGet(t.getCompletedAt().toInstant().getEpochSecond() - t.getStartedAt().toInstant().getEpochSecond());
                    tomatoTimes.incrementAndGet();
                })
                .collect(Collectors.groupingBy(t -> LocalDate.ofInstant(t.getCompletedAt().toInstant(), ZoneId.of("UTC+8")).toEpochDay(), TreeMap::new, Collectors.toList()));

        // 日均时长

        return Result.success(map);
    }
}
