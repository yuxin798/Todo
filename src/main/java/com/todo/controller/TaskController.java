package com.todo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.todo.dto.TaskDto;
import com.todo.service.TaskService;
import com.todo.vo.Result;
import com.todo.vo.TaskVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "任务API")
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "添加一项任务")
    @PostMapping("/")
    public Result<TaskVo> addTask(@Validated(TaskDto.AddTask.class) @RequestBody TaskDto taskDto) {
        TaskVo task = taskService.addTask(taskDto);
        return Result.success(task);
    }

    @Operation(summary = "删除一项任务")
    @DeleteMapping("/")
    public Result<?> removeTask(
            @NotNull(message = "任务id不能为null")
            @Min(value = 1, message = "任务id的最小值为1") Long taskId) {
        taskService.removeTask(taskId);
        return Result.success();
    }

    @Operation(summary = "修改一项任务")
    @PutMapping("/")
    public Result<TaskVo> updateTask(@Validated() @RequestBody TaskDto taskDto) {
        TaskVo task = taskService.updateTask(taskDto);
        return Result.success(task);
    }

    @Operation(summary = "分页查询任务")
    @GetMapping("/")
    public Result<Page<TaskVo>> findTaskPage(
            @ModelAttribute TaskDto taskDto,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int pageSize) {
        Page<TaskVo> taskPage = taskService.findTaskPage(taskDto, pageNum, pageSize);
        return Result.success(taskPage);
    }

    @Operation(summary = "查询一个任务")
    @GetMapping("/{taskId}")
    public Result<TaskVo> findById(
            @NotNull(message = "任务id不能为null")
            @Min(value = 1, message = "任务id的最小值为1")
            @PathVariable Long taskId) {
        TaskVo task = taskService.findById(taskId);
        return Result.success(task);
    }
}
