package com.todo.service.impl;

import com.todo.constant.RedisConstant;
import com.todo.service.StatisticService;
import com.todo.util.UserContextUtil;
import com.todo.vo.TaskVo;
import com.todo.vo.statistic.DayTomatoStatistic;
import com.todo.vo.statistic.StatisticVo;
import com.todo.vo.statistic.TaskRatio;
import lombok.Getter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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

    public StatisticServiceImpl(TaskServiceImpl taskServiceImpl, RedisTemplate<String, Object> redisTemplate) {
        this.taskServiceImpl = taskServiceImpl;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public StatisticVo statistic() {
        String key = RedisConstant.USER_STATISTIC + UserContextUtil.getUser().getUserId();

        Object o = redisTemplate.opsForValue().get(key);
        if (o != null) {
            StatisticVo statisticVo = (StatisticVo) o;
            statisticVo.setCached(true);
            return statisticVo;
        }

        List<TaskVo> tasks = taskServiceImpl.findAll();

        // 专注天数
        int tomatoDays;
        // 专注总时间次数
        AtomicInteger tomatoTimes = new AtomicInteger(0);
        // 专注总时间
        AtomicLong tomatoDuration = new AtomicLong(0);
        // 某天专注次数
        // 某天专注时长


        // 每天的任务分组
        HashMap<Long, List<TaskVo>> dayTomatoTmp = tasks.stream()
            .filter(t -> t.getTaskStatus() == 2)
            .peek(t -> {
                tomatoDuration.addAndGet((long) t.getTomatoClockTimes() * t.getClockDuration());
                tomatoTimes.addAndGet(t.getTomatoClockTimes());
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
                        dayTomatoStatistic.setTomatoTimes(t.getTomatoClockTimes());
                        dayTomatoStatistic.setTomatoDuration(t.getTomatoClockTimes() * t.getClockDuration().longValue());
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
        statisticVo.setRatioByDurationOfDay(getFocusDuration(dayTomatoTmp, DAY));
        statisticVo.setRatioByDurationOfWeek(getFocusDuration(dayTomatoTmp, WEEK));
        statisticVo.setRatioByDurationOfMonth(getFocusDuration(dayTomatoTmp, MONTH));

        // 日均时长
        tomatoDays = dayTomatoMap.size();
        statisticVo.setAvgTomatoTimes(tomatoDays == 0 ? 0 : tomatoTimes.get() / tomatoDays);
        statisticVo.setAvgTomatoDuration(tomatoDays == 0 ? 0 : tomatoDuration.get() / tomatoDays);

        statisticVo.setTomatoDays(tomatoDays);
        statisticVo.setTomatoTimes(tomatoTimes.get());
        statisticVo.setTomatoDuration(tomatoDuration.get());
        statisticVo.setDayTomatoMap(dayTomatoMap);
        statisticVo.setCached(true);

        redisTemplate.opsForValue().set(key, statisticVo, 5, TimeUnit.SECONDS);
        return statisticVo;
    }

    @Getter
    public enum Unit {
        DAY(1), WEEK(7), MONTH(30), YEAR(365);

        private final Integer days;

        Unit(Integer days) {
            this.days = days;
        }

        public long getStartEpochSecond() {
            switch (this) {
                case DAY -> {
                    return LocalDate.now(ZoneId.of("UTC+8")).toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"));
                }
                case WEEK -> {
                    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    return calendar.getTime().toInstant().getEpochSecond();
                }
                case MONTH -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                            0, 0, 0);
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                    return calendar.getTime().toInstant().getEpochSecond();
                }
                case YEAR -> {}
            }
            return 0;
        }

        public long getEndEpochSecond() {
            return LocalDate.now(ZoneId.of("UTC+8")).toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"));
        }

    }

    private List<TaskRatio> getFocusDuration(HashMap<Long, List<TaskVo>> dayTomatoTmp, Unit timeUnit) {
        // exclusive
        long start = 1000 * timeUnit.getStartEpochSecond();

        // inclusive
        long end = 1000 * timeUnit.getEndEpochSecond();

        Map<String, Integer> tmp = new HashMap<>();
        AtomicInteger sum = new AtomicInteger();
        dayTomatoTmp.forEach((k, v) -> {
            if (k >= start && k <= end) {
                v.forEach(t -> {
                    int duration = t.getClockDuration() * t.getTomatoClockTimes();
                    tmp.merge(t.getTaskName(), duration, Integer::sum);
                    sum.addAndGet(duration);
                });
            }
        });

        ArrayList<TaskRatio> taskRatios = new ArrayList<>();
        tmp.forEach((k, v) -> taskRatios.add(new TaskRatio(k, 1.0 * v / sum.get())));
        return taskRatios;
    }
}
