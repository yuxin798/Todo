package com.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TomatoClockDto {
    @NotBlank(message = "番茄钟ID不能为空", groups = {Completed.class})
    private Long clockId;

    @NotBlank(message = "任务状态不能为空", groups = {Completed.class})
    private Integer clockStatus;
    private String stopReason;
    private Date startedAt;
    @NotBlank(message = "完成时间不能为空", groups = {Completed.class})
    private Date completedAt;

    public interface Completed {}
}
