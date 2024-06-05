package com.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.constant.RedisConstant;
import com.todo.entity.Task;
import com.todo.entity.TomatoClock;
import com.todo.service.StatisticService;
import com.todo.service.TomatoClockService;
import com.todo.service.UserService;
import com.todo.util.UserContextUtil;
import com.todo.vo.Result;
import com.todo.vo.TaskVo;
import com.todo.vo.UserVo;
import com.todo.vo.statistic.DayTomatoStatistic;
import com.todo.vo.statistic.StatisticVo;
import com.todo.vo.statistic.StopReasonRatio;
import com.todo.vo.statistic.TaskRatio;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.todo.service.impl.StatisticServiceImpl.Unit.*;

@Service
public class StatisticServiceImpl implements StatisticService {
    private final TaskServiceImpl taskServiceImpl;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TomatoClockService tomatoClockService;
    private final HashMap<Long, String> taskIdNameMap;
    private final UserService userService;

    public StatisticServiceImpl(TaskServiceImpl taskServiceImpl, RedisTemplate<String, Object> redisTemplate, TomatoClockService tomatoClockService, UserService userService) {
        this.taskServiceImpl = taskServiceImpl;
        this.redisTemplate = redisTemplate;
        this.tomatoClockService = tomatoClockService;
        this.userService = userService;
        taskIdNameMap = new HashMap<>();
    }

    @Override
    public StatisticVo statistic(Long timestamp) {
        String key = RedisConstant.USER_STATISTIC + UserContextUtil.getUser().getUserId() + ":" + timestamp;

        Object o = redisTemplate.opsForValue().get(key);
        if (o != null) {
            StatisticVo statisticVo = (StatisticVo) o;
            statisticVo.setCached(true);
            return statisticVo;
        }

        List<TaskVo> tasks = taskServiceImpl.findAll();

        tasks.forEach(taskVo -> taskIdNameMap.put(taskVo.getTaskId(), taskVo.getTaskName()));

        List<TomatoClock> tomatoClocks;
        if (CollectionUtils.isEmpty(tasks)) {
            tomatoClocks = new ArrayList<>();
        } else {
            tomatoClocks = tomatoClockService.list(
                    new LambdaQueryWrapper<>(TomatoClock.class)
                            .in(TomatoClock::getTaskId, tasks
                                    .stream()
                                    .map(TaskVo::getTaskId)
                                    .collect(Collectors.toList()))
            );
        }

        StatisticVo statisticVo = getStatisticVo(tomatoClocks, timestamp);

        redisTemplate.opsForValue().set(key, statisticVo, 5, TimeUnit.MINUTES);
        return statisticVo;
    }

    @Override
    public StatisticVo statistic() {
        long timestamp = 1000 * LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC+8")).atStartOfDay().toEpochSecond(ZoneOffset.of("+8"));
        String key = RedisConstant.USER_STATISTIC + UserContextUtil.getUser().getUserId() + ":" + timestamp;

        Object o = redisTemplate.opsForValue().get(key);
        if (o != null) {
            StatisticVo statisticVo = (StatisticVo) o;
            statisticVo.setCached(true);
            return statisticVo;
        }
        List<TaskVo> tasks = taskServiceImpl.findAll();

        tasks.forEach(taskVo -> taskIdNameMap.put(taskVo.getTaskId(), taskVo.getTaskName()));

        List<TomatoClock> tomatoClocks;
        if (CollectionUtils.isEmpty(tasks)) {
            tomatoClocks = new ArrayList<>();
        } else {
            tomatoClocks = tomatoClockService.list(
                    new LambdaQueryWrapper<>(TomatoClock.class)
                            .in(TomatoClock::getTaskId, tasks
                                    .stream()
                                    .map(TaskVo::getTaskId)
                                    .collect(Collectors.toList()))
            );
        }
        StatisticVo statisticVo = getStatisticVo(tomatoClocks, timestamp);

        redisTemplate.opsForValue().set(key, statisticVo, 5, TimeUnit.MINUTES);
        return statisticVo;
    }

    @Override
    public List<UserVo> rankingList() {
        Object obj = redisTemplate.opsForValue().get(RedisConstant.RANKING_LIST);
        if (obj != null){
            if (obj instanceof List<?>){
                return (List<UserVo>) obj;
            }

        }

        List<UserVo> userVos = rankingListStatistic();

        redisTemplate.opsForValue().set(RedisConstant.RANKING_LIST, userVos, 25, TimeUnit.HOURS);
        return userVos;
    }

    @NotNull
    public List<UserVo> rankingListStatistic() {
        return userService.list()
                .stream()
                .map(UserVo::new)
                .peek(u -> {
                    List<Long> parentIds = taskServiceImpl.list(new LambdaQueryWrapper<>(Task.class)
                                    .eq(Task::getUserId, u.getUserId())
                                    .eq(Task::getTaskStatus, Task.Status.COMPLETED.getCode()))
                            .stream()
                            .map(Task::getParentId)
                            .distinct()
                            .toList();
                    AtomicLong tomatoDuration = new AtomicLong(0);
                    if (!parentIds.isEmpty()) {
                        tomatoClockService.list(new LambdaQueryWrapper<>(TomatoClock.class)
                                        .in(TomatoClock::getParentId, parentIds)
                                        .eq(TomatoClock::getClockStatus, TomatoClock.Status.COMPLETED.getCode()))
                                .forEach(t -> tomatoDuration.addAndGet(t.getClockDuration()));
                    }
                    u.setTomatoDuration(tomatoDuration.get());
                })
                .sorted(Comparator.comparing(UserVo::getTomatoDuration).reversed())
                .limit(50)
                .collect(Collectors.toList());
    }

    @Override
    public Result<List<StopReasonRatio>> statisticStopReason() {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getUserId, UserContextUtil.getUserId());
        List<Long> taskIds = taskServiceImpl.list(queryWrapper)
                .stream()
                .map(Task::getTaskId)
                .distinct()
                .toList();

        if (CollectionUtils.isEmpty(taskIds)){
            return Result.success(new ArrayList<>());
        }

        List<TomatoClock> tomatoClocks = tomatoClockService.list(new LambdaQueryWrapper<>(TomatoClock.class)
                .in(TomatoClock::getTaskId, taskIds)
                .eq(TomatoClock::getClockStatus, TomatoClock.Status.TERMINATED.getCode())
                .isNotNull(TomatoClock::getStopReason)
                .ne(TomatoClock::getStopReason, ""));

        int sum = tomatoClocks.size();
        List<StopReasonRatio> stopReasonRatios = tomatoClocks.stream()
                .collect(Collectors.groupingBy(
                        TomatoClock::getStopReason,
                        Collectors.summingDouble(tc -> 1.0 / sum)
                ))
                .entrySet().stream()
                .map(entry -> new StopReasonRatio(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return Result.success(stopReasonRatios);
    }

    @Override
    public DayTomatoStatistic simpleStatisticToday() {
        LocalDate date = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC+8"));
        Date start = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"))));
        Date end = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MAX, ZoneOffset.of("+8"))));

        List<Long> taskIds = taskServiceImpl.list(new LambdaQueryWrapper<>(Task.class)
                        .eq(Task::getUserId, UserContextUtil.getUserId())
                        .eq(Task::getTaskStatus, Task.Status.COMPLETED.getCode())
                        .ge(Task::getCreatedAt, start)
                        .le(Task::getCreatedAt, end))
                .stream()
                .map(Task::getTaskId)
                .distinct()
                .toList();
        if (taskIds.isEmpty()) {
            return new DayTomatoStatistic();
        }

        List<TomatoClock> tomatoClocks = tomatoClockService.list(new LambdaQueryWrapper<>(TomatoClock.class)
                .in(TomatoClock::getTaskId, taskIds)
                .eq(TomatoClock::getClockStatus, TomatoClock.Status.COMPLETED.getCode()));

        AtomicLong tomatoDuration = new AtomicLong(0);
        tomatoClocks.forEach(t -> tomatoDuration.addAndGet(t.getClockDuration()));

        DayTomatoStatistic dayTomatoStatistic = new DayTomatoStatistic();
        dayTomatoStatistic.setTomatoTimes(tomatoClocks.size());
        dayTomatoStatistic.setTomatoDuration(tomatoDuration.get());
        return dayTomatoStatistic;
    }

    @Override
    public StatisticVo statisticByTask(Long taskId, Long timestamp) {
        String key = RedisConstant.TASK_STATISTIC + taskId + ":" + timestamp;

        Object o = redisTemplate.opsForValue().get(key);
        if (o != null) {
            StatisticVo statisticVo = (StatisticVo) o;
            statisticVo.setCached(true);
            return statisticVo;
        }

        Task task = taskServiceImpl.getById(taskId);
        Long parentId = task.getParentId();
        List<Task> tasks = taskServiceImpl.list(new LambdaQueryWrapper<>(Task.class)
                .eq(Task::getParentId, parentId)
        );


        tasks.forEach(t -> taskIdNameMap.put(t.getTaskId(), t.getTaskName()));

        List<TomatoClock> tomatoClocks = tomatoClockService.list(
                new LambdaQueryWrapper<>(TomatoClock.class)
                        .eq(TomatoClock::getParentId, task.getParentId())
        );
        StatisticVo statisticVo = getStatisticVo(tomatoClocks, timestamp);

        redisTemplate.opsForValue().set(key, statisticVo, 1, TimeUnit.MINUTES);
        return statisticVo;
    }

    @Override
    public StatisticVo simpleStatisticByTask(Long taskId) {
        Task task = taskServiceImpl.getById(taskId);

        StatisticVo statisticVo = new StatisticVo();
        AtomicInteger tomatoTimes = new AtomicInteger(0);
        AtomicLong tomatoDuration = new AtomicLong(0);

        LambdaQueryWrapper<TomatoClock> queryWrapper = new LambdaQueryWrapper<>(TomatoClock.class)
                .eq(TomatoClock::getParentId, task.getParentId())
                .eq(TomatoClock::getClockStatus, TomatoClock.Status.COMPLETED.getCode());
        tomatoClockService.list(queryWrapper)
                .forEach(t -> {
                    tomatoTimes.addAndGet(1);
                    tomatoDuration.addAndGet(t.getClockDuration());
                });
        statisticVo.setTomatoTimes(tomatoTimes.get());
        statisticVo.setTomatoDuration(tomatoDuration.get());
        return statisticVo;
    }

    @NotNull
    private StatisticVo getStatisticVo(List<TomatoClock> tomatoClocks, Long timestamp) {
        // 专注天数
        int tomatoDays;
        // 专注总时间次数
        AtomicInteger tomatoTimes = new AtomicInteger(0);
        // 专注总时间
        AtomicLong tomatoDuration = new AtomicLong(0);
        // 某天专注次数
        // 某天专注时长

        // 每天的任务分组
        HashMap<Long, List<TomatoClock>> dayTomatoTmp = tomatoClocks.stream()
                .filter(t -> t.getClockStatus() == 0)
                .peek(t -> {
                    tomatoDuration.addAndGet(t.getClockDuration());
                    tomatoTimes.addAndGet(1);
                })
                .collect(Collectors.groupingBy(
                        // 获取某一天的时间戳，并通过这个分组
                        t -> 1000 * LocalDate.ofInstant(t.getCompletedAt().toInstant(), ZoneId.of("UTC+8")).atStartOfDay().toEpochSecond(ZoneOffset.of("+8")),
                        HashMap::new,
                        Collectors.toList())
                );

        // 每天的总专注
        Map<Long, DayTomatoStatistic> dayTomatoMap = new HashMap<>();
        dayTomatoTmp.forEach((k, v) -> {
            DayTomatoStatistic dayTomato = v.stream()
                    .map(t -> {
                        DayTomatoStatistic dayTomatoStatistic = new DayTomatoStatistic();
                        dayTomatoStatistic.setTomatoTimes(1);
                        dayTomatoStatistic.setTomatoDuration(t.getClockDuration().longValue());
                        return dayTomatoStatistic;
                    })
                    .reduce(new DayTomatoStatistic(0, 0L), (s1, s2) -> {
                        s1.setTomatoTimes(s1.getTomatoTimes() + s2.getTomatoTimes());
                        s1.setTomatoDuration(s1.getTomatoDuration() + s2.getTomatoDuration());
                        return s1;
                    });
            dayTomatoMap.put(k, dayTomato);
        });

        // 整合数据
        StatisticVo statisticVo = new StatisticVo();
        // 专注时长分布
        statisticVo.setRatioByDurationOfDay(getFocusDuration(taskIdNameMap, dayTomatoTmp, timestamp, DAY));
        statisticVo.setRatioByDurationOfWeek(getFocusDuration(taskIdNameMap, dayTomatoTmp,timestamp, WEEK));
        statisticVo.setRatioByDurationOfMonth(getFocusDuration(taskIdNameMap, dayTomatoTmp, timestamp,MONTH));

        // 日均时长
        tomatoDays = dayTomatoMap.size();
        statisticVo.setAvgTomatoTimes(tomatoDays == 0 ? 0 : tomatoTimes.get() / tomatoDays);
        statisticVo.setAvgTomatoDuration(tomatoDays == 0 ? 0 : tomatoDuration.get() / tomatoDays);

        statisticVo.setTomatoDays(tomatoDays);
        statisticVo.setTomatoTimes(tomatoTimes.get());
        statisticVo.setTomatoDuration(tomatoDuration.get());
        statisticVo.setDayTomatoMap(dayTomatoMap);
        statisticVo.setCached(true);
        return statisticVo;
    }

    @Getter
    public enum Unit {
        DAY(1), WEEK(7), MONTH(30), YEAR(365);

        private final Integer days;

        Unit(Integer days) {
            this.days = days;
        }

        public long getStartEpochSecond(Long timestamp) {
            switch (this) {
                case DAY -> {
                    return LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC+8")).toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"));
                }
                case WEEK -> {
                    Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
                    calendar.setTimeInMillis(timestamp);
                    if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
                        calendar.add(Calendar.DATE, -1);
                    }
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    return calendar.getTime().toInstant().getEpochSecond();
                }
                case MONTH -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    calendar.set(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                            0, 0, 0);
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    return calendar.getTime().toInstant().getEpochSecond();
                }
                case YEAR -> {
                }
            }
            return 0;
        }

        public long getEndEpochSecond(Long timestamp) {
            switch (this) {
                case DAY -> {
                    return LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC+8")).toEpochSecond(LocalTime.MAX, ZoneOffset.of("+8"));
                }
                case WEEK -> {
                    Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
                    calendar.setTimeInMillis(timestamp);
                    if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
                        calendar.add(Calendar.DATE, 7);
                    }
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    return calendar.getTime().toInstant().getEpochSecond();
                }
                case MONTH -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    calendar.set(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                            0, 0, 0);
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    return calendar.getTime().toInstant().getEpochSecond();
                }
                case YEAR -> {
                }
            }
            return 0;
        }
    }

    private List<TaskRatio> getFocusDuration(HashMap<Long, String> taskIdNameMap, HashMap<Long, List<TomatoClock>> dayTomatoTmp, Long timestamp, Unit timeUnit) {
        // exclusive
        long start = 1000 * timeUnit.getStartEpochSecond(timestamp);

        // inclusive
        long end = 1000 * timeUnit.getEndEpochSecond(timestamp);

        Map<String, Integer> tmp = new HashMap<>();
        AtomicInteger sum = new AtomicInteger();
        dayTomatoTmp.forEach((k, v) -> {
            if (k >= start && k <= end) {
                v.forEach(t -> {
                    int duration = t.getClockDuration();
                    tmp.merge(taskIdNameMap.get(t.getTaskId()), duration, Integer::sum);
                    sum.addAndGet(duration);
                });
            }
        });

        ArrayList<TaskRatio> taskRatios = new ArrayList<>();
        tmp.forEach((k, v) -> taskRatios.add(new TaskRatio(k, 1.0 * v / sum.get())));
        return taskRatios;
    }
}
