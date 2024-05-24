package com.todo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCategoryDto {
    @NotNull(message = "任务分类Id不能为空", groups = UpdateTaskCategory.class)
    private Long categoryId;
    @NotNull(message = "任务分类名称不能未空", groups = AddTaskCategory.class)
    private String categoryName;
    @NotNull(message = "任务分类颜色不能未空", groups = AddTaskCategory.class)
    private Integer color;

    public interface AddTaskCategory {}
    public interface UpdateTaskCategory {}
}
