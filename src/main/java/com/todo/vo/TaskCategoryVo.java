package com.todo.vo;

import com.todo.entity.TaskCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCategoryVo {
    private Long categoryId;
    private String categoryName;
    private Integer color;

    public TaskCategoryVo(TaskCategory taskCategory) {
        this.categoryId = taskCategory.getCategoryId();
        this.categoryName = taskCategory.getCategoryName();
        this.color = taskCategory.getColor();
    }
}
