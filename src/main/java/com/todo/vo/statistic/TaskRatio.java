package com.todo.vo.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRatio {
    private String taskName;                    // 任务名
    private Double ratio;                       // 比例
}
