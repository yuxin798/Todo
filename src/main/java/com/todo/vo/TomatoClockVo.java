package com.todo.vo;

import com.todo.entity.TomatoClock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TomatoClockVo {
    private Long clockId;
    private Long taskId;
    private Integer sequence;
    private Integer clockDuration;
    private Integer clockStatus;
    private String stopReason;
    private Integer innerInterrupt;
    private Integer outerInterrupt;
    private Date startedAt;
    private Date completedAt;

    public TomatoClockVo(TomatoClock tomatoClock) {
        this.clockId = tomatoClock.getClockId();
        this.taskId = tomatoClock.getTaskId();
        this.sequence = tomatoClock.getSequence();
        this.clockDuration = tomatoClock.getClockDuration();
        this.clockStatus = tomatoClock.getClockStatus();
        this.stopReason = tomatoClock.getStopReason();
        this.innerInterrupt = tomatoClock.getInnerInterrupt();
        this.outerInterrupt = tomatoClock.getOuterInterrupt();
        this.startedAt = tomatoClock.getStartedAt();
        this.completedAt = tomatoClock.getCompletedAt();
    }
}
