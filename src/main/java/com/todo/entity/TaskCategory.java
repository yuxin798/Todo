package com.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.todo.dto.TaskCategoryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@TableName(value ="task_category")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCategory implements Serializable {
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;
    private String categoryName;
    private Long userId;
    private Integer color;
    private Date createdAt;
    private Date updatedAt;
    private Integer deleted;

    public TaskCategory(TaskCategoryDto taskCategoryDto){
        this.categoryName = taskCategoryDto.getCategoryName();
        this.color = taskCategoryDto.getColor();
    }

    @Override
    public String toString() {
        return "TaskCategory{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", color=" + color +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deleted=" + deleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskCategory that = (TaskCategory) o;
        return Objects.equals(categoryId, that.categoryId) && Objects.equals(categoryName, that.categoryName) && Objects.equals(color, that.color) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(deleted, that.deleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId, categoryName, color, createdAt, updatedAt, deleted);
    }
}
