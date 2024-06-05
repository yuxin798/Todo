package com.todo.service;

import com.todo.vo.Result;
import com.todo.vo.UserVo;
import com.todo.vo.statistic.DayTomatoStatistic;
import com.todo.vo.statistic.StatisticVo;
import com.todo.vo.statistic.StopReasonRatio;

import java.util.List;

public interface StatisticService {
    StatisticVo statisticByTask(Long taskId, Long timestamp);

    StatisticVo simpleStatisticByTask(Long taskId);

    StatisticVo statistic(Long timestamp);

    StatisticVo statistic();

    List<UserVo> rankingList();

    List<UserVo> rankingListStatistic();

    Result<List<StopReasonRatio>> statisticStopReason();

    DayTomatoStatistic simpleStatisticToday();
}
