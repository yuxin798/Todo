package com.todo.schedule;

import com.todo.constant.RedisConstant;
import com.todo.service.StatisticService;
import com.todo.vo.UserVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StatisticSchedule {

    private final StatisticService statisticService;
    private final RedisTemplate<String, Object> redisTemplate;

    public StatisticSchedule(StatisticService statisticService, RedisTemplate<String, Object> redisTemplate) {
        this.statisticService = statisticService;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 58 11 * * ?")
    public void statisticScheduleQuery() {
        List<UserVo> userVos = statisticService.rankingListStatistic();
        redisTemplate.opsForValue().set(RedisConstant.RANKING_LIST_TEMP, userVos);
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void statisticScheduleUpdate() {
        Object o = redisTemplate.opsForValue().get(RedisConstant.RANKING_LIST_TEMP);
        redisTemplate.opsForValue().set(RedisConstant.RANKING_LIST, o);
    }
}
