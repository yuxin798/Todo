package com.todo.service;

import com.todo.vo.statistic.StatisticVo;

public interface StatisticService {
    StatisticVo statisticByTask(Long taskId, Long timestamp);

    StatisticVo simpleStatisticByTask(Long taskId);

    StatisticVo statistic(Long timestamp);

    StatisticVo statistic();
}
