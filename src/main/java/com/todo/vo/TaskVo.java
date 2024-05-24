package com.todo.vo;

import com.todo.entity.Task;
import com.todo.util.ListUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskVo {
    private Long taskId;
    private Long userId;

    private String taskName;
    private Integer type;
    private Integer clockDuration;
    private Integer taskStatus;
    private String remark;
    private List<Integer> estimate;
    private Integer restTime;
    private Integer again;

    private Integer todayTotalTimes;

    private Long categoryId;
    private Integer tomatoClockTimes;
    private List<TomatoClockVo> tomatoClocks;
    private Integer stopTimes;

    private String background;
    private Integer innerInterrupt;
    private Integer outerInterrupt;

    private Date startedAt;
    private Date completedAt;

    private Date createdAt;


    public TaskVo(Task task) {
        taskId = task.getTaskId();
        userId = task.getUserId();
        taskName = task.getTaskName();
        type = task.getType();
        clockDuration = task.getClockDuration();
        remark = task.getRemark();
        estimate = ListUtil.commaSeparateStringToList(task.getEstimate(), Integer::valueOf);
        restTime = task.getRestTime();
        again = task.getAgain();
        todayTotalTimes = task.getTodayTotalTimes();
        categoryId = task.getCategoryId();
        tomatoClockTimes = task.getTomatoClockTimes();
        stopTimes = task.getStopTimes();
        taskStatus = task.getTaskStatus();
        background = task.getBackground();
        innerInterrupt = task.getInnerInterrupt();
        outerInterrupt = task.getOuterInterrupt();
        startedAt = task.getStartedAt();
        completedAt = task.getCompletedAt();
        createdAt = task.getCreatedAt();
    }

    public void setTomatoClocks(List<TomatoClockVo> tomatoClocks) {
        this.tomatoClocks = tomatoClocks;
    }

}
