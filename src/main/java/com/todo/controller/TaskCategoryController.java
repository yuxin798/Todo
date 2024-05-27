package com.todo.controller;

import com.todo.dto.TaskCategoryDto;
import com.todo.entity.TaskCategory;
import com.todo.service.TaskCategoryService;
import com.todo.vo.Result;
import com.todo.vo.TaskCategoryVo;
import com.todo.vo.TaskVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "清单任务分类API")
@RestController
@RequestMapping("/category")
public class TaskCategoryController {

    private final TaskCategoryService taskCategoryService;

    public TaskCategoryController(TaskCategoryService taskCategoryService) {
        this.taskCategoryService = taskCategoryService;
    }

    @Operation(summary = "新增清单分类")
    @PostMapping("/")
    public Result<TaskCategoryVo> addTaskCategory(@RequestBody @Validated(TaskCategoryDto.AddTaskCategory.class) TaskCategoryDto taskCategoryDto){
        return taskCategoryService.addTaskCategory(taskCategoryDto);
    }

    @Operation(summary = "删除清单分类")
    @DeleteMapping("/{categoryId}")
    public Result<?> deleteTaskCategory(@PathVariable Long categoryId){
        return taskCategoryService.deleteTaskCategory(categoryId);
    }

    @Operation(summary = "修改清单分类")
    @PutMapping("/")
    public Result<TaskCategoryVo> updateTaskCategory(@RequestBody @Validated(TaskCategoryDto.UpdateTaskCategory.class) TaskCategoryDto taskCategoryDto){
        return taskCategoryService.updateTaskCategory(taskCategoryDto);
    }

    @Operation(summary = "查询所有清单集合")
    @GetMapping("/")
    public Result<List<TaskCategoryVo>> getAll(){
        return taskCategoryService.getAll();
    }

    @Operation(summary = "查询一个清单的所有任务")
    @GetMapping("/{categoryId}")
    public Result<List<TaskVo>> getAllTasks(@PathVariable Long categoryId){
        return taskCategoryService.getAllTasks(categoryId);
    }
}
