package com.todo.vo;

import com.todo.entity.Task;
import com.todo.entity.TomatoClock;
import com.todo.util.ListUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskVo {
    private Long taskId;
    private Long userId;

    private String taskName;
    private List<Integer> estimate;

    private Integer tomatoClockTimes;
    private List<TomatoClock> tomatoClocks;
    private Integer stopTimes;

    private Integer taskStatus;
    private Integer innerInterrupt;
    private Integer outerInterrupt;

    private Date startedAt;
    private Date completedAt;

    public TaskVo(Task task) {
        taskId = task.getTaskId();
        userId = task.getUserId();
        taskName = task.getTaskName();
        estimate = ListUtil.commaSeparateStringToList(task.getEstimate(), Integer::valueOf);
        tomatoClockTimes = task.getTomatoClockTimes();
        stopTimes = task.getStopTimes();
        taskStatus = task.getTaskStatus();
        innerInterrupt = task.getInnerInterrupt();
        outerInterrupt = task.getOuterInterrupt();
        startedAt = task.getStartedAt();
        completedAt = task.getCompletedAt();
    }
}
