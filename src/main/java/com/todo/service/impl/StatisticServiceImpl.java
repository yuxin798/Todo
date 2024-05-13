package com.todo.service.impl;

import com.todo.constant.RedisConstant;
import com.todo.service.StatisticService;
import com.todo.util.UserContextUtil;
import com.todo.vo.TaskVo;
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
    public Map<String, Object> statistic() {
        String key = RedisConstant.USER_STATISTIC + UserContextUtil.getUser().getUserId();

        Object o = redisTemplate.opsForValue().get(key);
        if (o != null) {
            Map<String, Object> map = (Map<String, Object>) o;
            map.put("cached", true);
            return map;
        }

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


        // 每天的任务分组
        HashMap<Long, List<TaskVo>> dayTomatoTmp = tasks.stream()
            .filter(t -> t.getTaskStatus() == 2)
            .peek(t -> {
                tomatoDuration.addAndGet((long) t.getTomatoClockTimes() * t.getClockDuration());
                tomatoTimes.addAndGet(t.getTomatoClockTimes());
            })
            .collect(Collectors.groupingBy(
                    // 获取某一天的时间戳，并通过这个分组
                    t -> LocalDate.ofInstant(t.getCompletedAt().toInstant(), ZoneId.of("UTC+8")).toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8")),
                    HashMap::new,
                    Collectors.toList())
            );

        HashMap<Long, Map<String, Long>> dayTomato = new HashMap<>();

        // 每天的总专注
        dayTomatoTmp.forEach((k, v) -> {
            HashMap<String, Long> res = v.stream()
                    .map(t -> {
                        HashMap<String, Long> m = new HashMap<>();
                        m.put("tomatoTimes", Long.valueOf(t.getTomatoClockTimes()));
                        m.put("tomatoDuration", (long) t.getTomatoClockTimes() * t.getClockDuration());
                        return m;
                    })
                    .reduce(new HashMap<>(), (m1, m2) -> {
                        m1.merge("tomatoTimes", m2.get("tomatoTimes"), Long::sum);
                        m1.merge("tomatoDuration", m2.get("tomatoDuration"), Long::sum);
                        return m1;
                    });
            dayTomato.put(k, res);
        });

        // 专注时长分布
        map.put("durationByDay", getFocusDuration(dayTomatoTmp, DAY));
        map.put("durationByWeek", getFocusDuration(dayTomatoTmp, WEEK));
        map.put("durationByMonth", getFocusDuration(dayTomatoTmp, MONTH));

        // 日均时长
        tomatoDays = dayTomato.size();
        avgTomatoDuration = tomatoDays == 0 ? 0 : tomatoDuration.get() / tomatoDays;
        avgTomatoTimes = tomatoDays == 0 ? 0 : tomatoTimes.get() / tomatoDays;

        map.put("tomatoDays", tomatoDays);
        map.put("avgTomatoTimes", avgTomatoTimes);
        map.put("avgTomatoDuration", avgTomatoDuration);
        map.put("tomatoTimes", tomatoTimes.get());
        map.put("tomatoDuration", tomatoDuration.get());
        map.put("dayTomato", dayTomato);
        map.put("cached", false);

        redisTemplate.opsForValue().set(key, map, 5, TimeUnit.SECONDS);
        return map;
    }

    @Getter
    enum Unit {
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

    private Map<String, Double> getFocusDuration(HashMap<Long, List<TaskVo>> dayTomatoTmp, Unit timeUnit) {
        // exclusive
        long start = timeUnit.getStartEpochSecond();

        // inclusive
        long end = timeUnit.getEndEpochSecond();

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

        HashMap<String, Double> res = new HashMap<>();
        tmp.forEach((k, v) -> res.put(k, 1.0 * v / sum.get()));
        return res;
    }
}
