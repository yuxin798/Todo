package com.todo.dto;

import com.todo.constraints.ValidName;
import com.todo.entity.TomatoClock;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {
    @NotNull(message = "任务id不能为null", groups = UpdateTask.class)
    @Min(value = 1, message = "任务id的最小值为1")
    private Long taskId;
    private Long userId;

    @ValidName(min = 1, max = 32, message = "任务名必须为1~32个字符", groups = {AddTask.class, UpdateTask.class})
    private String taskName;

    @Size(min = 1, message = "预估番茄钟数不能为0", groups = {AddTask.class, UpdateTask.class})
    @NotNull(message = "预估番茄钟数不能为null", groups = AddTask.class)
    private List<Integer> estimate;

    @NotNull(message = "任务id不能为null", groups = AddTask.class)
    @Min(value = 1, message = "番茄钟时长最小为1", groups = {AddTask.class, UpdateTask.class})
    private Integer clockDuration;

    private String category;

    @Min(value = 1, message = "番茄钟数最小值为1", groups = UpdateTask.class)
    private Integer tomatoClockTimes;
    private List<TomatoClock> tomatoClocks;
    @Min(value = 1, message = "番茄钟数最小值为0", groups = UpdateTask.class)
    private Integer stopTimes;

    @Min(value = 0, message = "任务状态范围为0~3", groups = UpdateTask.class)
    @Max(value = 3, message = "任务状态范围为0~3", groups = UpdateTask.class)
    private Integer taskStatus;

    @Min(value = 0, message = "最小内部中断次数为0", groups = UpdateTask.class)
    private Integer innerInterrupt;
    @Min(value = 0, message = "最小外部中断次数为0", groups = UpdateTask.class)
    private Integer outerInterrupt;

    private Date startedAt;
    private Date completedAt;

    public interface AddTask {}
    public interface UpdateTask {}
}
