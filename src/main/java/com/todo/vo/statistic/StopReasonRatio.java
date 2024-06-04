package com.todo.vo.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StopReasonRatio {
    private String stopReason;   // 中断原因
    private Double ratio;
}
