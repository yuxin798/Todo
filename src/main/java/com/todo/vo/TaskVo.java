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
    private Long parentId;
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
    private String background;
    private Date startedAt;
    private Date completedAt;
    private Date createdAt;


    public TaskVo(Task task) {
        taskId = task.getTaskId();
        parentId = task.getParentId();
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
        taskStatus = task.getTaskStatus();
        background = task.getBackground();
        startedAt = task.getStartedAt();
        completedAt = task.getCompletedAt();
        createdAt = task.getCreatedAt();
    }
}
