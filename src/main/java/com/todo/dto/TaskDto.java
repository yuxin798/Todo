package com.todo.dto;

import com.todo.entity.TomatoClock;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

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
    private Long parentId;;

//    @ValidName(min = 1, max = 32, message = "任务名必须为1~32个字符", groups = {AddTask.class, UpdateTask.class})
    @NotBlank(message = "任务名不能为空", groups = AddTask.class)
    @Length(min = 1, max = 32, message = "任务名必须为1~32个字符", groups = {AddTask.class, UpdateTask.class})
    private String taskName;

    @NotNull(message = "计时类型不能为空", groups = AddTask.class)
    @Range(min = 0, max = 2, message = "计时类型只能取值0,1,2", groups = {AddTask.class, UpdateTask.class})
    private Integer type;

    private Integer clockDuration;

    private String remark;

    private List<Integer> estimate;

    @Min(value = 1, message = "自定义休息时间最小值为1分钟", groups = {AddTask.class, UpdateTask.class})
    private Integer restTime;

    @Range(min = 0, max = 1, message = "第二天是否再次显示只能为0或1", groups = {AddTask.class, UpdateTask.class})
    private Integer again;

    private Long categoryId;

    @Min(value = 0, message = "任务状态范围为0~3", groups = UpdateTask.class)
    @Max(value = 3, message = "任务状态范围为0~3", groups = UpdateTask.class)
    private Integer taskStatus;

    private String background;


    private Date startedAt;
    private Date completedAt;

    public interface AddTask {}
    public interface UpdateTask {}
}
