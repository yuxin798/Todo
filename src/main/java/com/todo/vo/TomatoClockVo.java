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
    private Long parentId;
    private Integer sequence;
    private Integer clockDuration;
    private Integer clockStatus;
    private String stopReason;
    private Date startedAt;
    private Date completedAt;

    private String taskName;

    public TomatoClockVo(TomatoClock tomatoClock) {
        this.clockId = tomatoClock.getClockId();
        this.taskId = tomatoClock.getTaskId();
        this.parentId = tomatoClock.getParentId();
        this.sequence = tomatoClock.getSequence();
        this.clockDuration = tomatoClock.getClockDuration();
        this.clockStatus = tomatoClock.getClockStatus();
        this.stopReason = tomatoClock.getStopReason();
        this.startedAt = tomatoClock.getStartedAt();
        this.completedAt = tomatoClock.getCompletedAt();
    }

    public TomatoClockVo(TomatoClock tomatoClock, String taskName) {
        this.clockId = tomatoClock.getClockId();
        this.taskId = tomatoClock.getTaskId();
        this.parentId = tomatoClock.getParentId();
        this.sequence = tomatoClock.getSequence();
        this.clockDuration = tomatoClock.getClockDuration();
        this.clockStatus = tomatoClock.getClockStatus();
        this.stopReason = tomatoClock.getStopReason();
        this.startedAt = tomatoClock.getStartedAt();
        this.completedAt = tomatoClock.getCompletedAt();
        this.taskName = taskName;
    }
}
