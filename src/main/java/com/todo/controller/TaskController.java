package com.todo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.todo.dto.TaskDto;
import com.todo.entity.Task;
import com.todo.service.TaskService;
import com.todo.util.UserContextUtil;
import com.todo.vo.Result;
import com.todo.vo.TaskVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

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
    public Result<TaskVo> updateTask(@Validated(TaskDto.UpdateTask.class) @RequestBody TaskDto taskDto) {
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

    @Operation(summary = "查询用户的全部任务")
    @GetMapping("/all")
    public Result<List<TaskVo>> findAll() {
        return Result.success(taskService.findAll());
    }

    @Operation(summary = "查询一个类别的任务")
    @GetMapping("/findByCategory/{category}")
    public Result<List<TaskVo>> findByCategory(@PathVariable String category) {
        return taskService.findByCategory(category);
    }

    @Operation(summary = "查询某一天的任务")
    @GetMapping("/findByDay")
    public Result<List<TaskVo>> findByDay(@RequestParam Long timestamp) {
        return taskService.findByDay(timestamp);
    }

    @Operation(summary = "完成任务，同步番茄钟与任务数据")
    @PutMapping("/complete/{taskId}")
    public Result<?> complete(@PathVariable Long taskId) {
        return taskService.complete(taskId);
    }
}
