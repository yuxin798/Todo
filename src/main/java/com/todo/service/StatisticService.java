package com.todo.service;

import com.todo.vo.statistic.StatisticVo;

public interface StatisticService {
    StatisticVo statistic();

    StatisticVo statisticByTask(Long taskId);

    StatisticVo simpleStatisticByTask(Long taskId);
}
