package com.todo.vo.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 某日番茄钟的统计
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DayTomatoStatistic {
    private Integer tomatoTimes;                // 番茄钟数（专注次数）
    private Long tomatoDuration;             // 番茄钟时长（专注时长）
}