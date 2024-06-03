package com.todo.vo;

import com.todo.entity.TaskCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCategoryVo {
    private Long categoryId;
    private String categoryName;
    private Integer color;

    private List<TaskVo> taskVos;

    public TaskCategoryVo(TaskCategory taskCategory) {
        this.categoryId = taskCategory.getCategoryId();
        this.categoryName = taskCategory.getCategoryName();
        this.color = taskCategory.getColor();
    }
}
