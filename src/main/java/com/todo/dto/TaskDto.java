package com.todo.dto;

import com.todo.entity.TomatoClock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {
    private Long taskId;
    private Long userId;

    @NotBlank(message = "任务名不能为空", groups = {AddTask.class})
    private String taskName;

    @Size(min = 1, message = "预估番茄钟数不能为0", groups = {AddTask.class})
    private List<Integer> estimate;

    private Integer tomatoClockTimes;
    private List<TomatoClock> tomatoClocks;
    private Integer stopTimes;

    private Integer taskStatus;
    private Integer innerInterrupt;
    private Integer outerInterrupt;

    private Date startedAt;
    private Date completedAt;

    public interface AddTask {

    }
}
