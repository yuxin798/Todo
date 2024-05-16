package com.todo.vo.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 数据统计类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticVo {
    private Integer tomatoTimes;                                // 总番茄钟数（总专注次数）
    private Long tomatoDuration;                                // 总番茄钟时长（总专注时长）
    private Map<Long, DayTomatoStatistic> dayTomatoMap;         // 某日番茄钟统计
    private Long avgTomatoDuration;                             // 每天平均专注时长
    private Integer avgTomatoTimes;                             // 每天平均专注次数
    private Boolean cached;                                     // 是否是Redis缓存
    private Integer tomatoDays;                                 // 一共使用的天数
    private List<TaskRatio> ratioByDurationOfDay;               // 通过专注时长 按天
    private List<TaskRatio> ratioByDurationOfWeek;              // 通过专注时长 按周
    private List<TaskRatio> ratioByDurationOfMonth;             // 通过专注时长 按月
}
