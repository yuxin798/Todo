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

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

        // 专注天数
        long tomatoDays;
        // 日均专注时长
        long avgTomatoDuration;
        // 日均专注番茄数
        long avgTomatoTimes;
        // 专注总时间次数
        AtomicLong tomatoTimes = new AtomicLong(0);
        // 专注总时间
        AtomicLong tomatoDuration = new AtomicLong(0);
        // 某天专注次数
        // 某天专注时长
        TreeMap<Long, Map<String, Long>> dayTomato;

        dayTomato = tasks.stream()
                .parallel()
                .flatMap(t -> t.getTomatoClocks().stream())
                .filter(t -> t.getClockStatus() == 0)
                .peek(t -> {
                    tomatoDuration.addAndGet(t.getCompletedAt().toInstant().getEpochSecond() - t.getStartedAt().toInstant().getEpochSecond());
                    tomatoTimes.incrementAndGet();
                })
                .collect(Collectors.groupingBy(
                    t -> LocalDate.ofInstant(t.getCompletedAt().toInstant(), ZoneId.of("UTC+8")).toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8")),
                    TreeMap::new,
                    Collectors.mapping(t -> {
                        HashMap<String, Long> m = new HashMap<>();
                        m.put("tomatoTimes", 1L);
                        m.put("tomatoDuration", t.getCompletedAt().toInstant().getEpochSecond() - t.getStartedAt().toInstant().getEpochSecond());
                        return m;
                    }, Collectors.reducing(
                            new HashMap<>(),
                            (a, b) -> {
                                a.merge("tomatoTimes", b.get("tomatoTimes"), Long::sum);
                                a.merge("tomatoDuration", b.get("tomatoDuration"), Long::sum);
                                return a;
                            }
                    ))
                ));

        // 日均时长
        tomatoDays = dayTomato.size();
        avgTomatoDuration = tomatoDuration.get() / tomatoDays;
        avgTomatoTimes = tomatoTimes.get() / tomatoDays;

        map.put("tomatoDays", tomatoDays);
        map.put("avgTomatoTimes", avgTomatoTimes);
        map.put("avgTomatoDuration", avgTomatoDuration);
        map.put("tomatoTimes", tomatoTimes.get());
        map.put("tomatoDuration", tomatoDuration.get());
        map.put("dayTomato", dayTomato);

        return Result.success(map);
    }
}
