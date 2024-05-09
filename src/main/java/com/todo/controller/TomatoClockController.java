package com.todo.controller;

import com.todo.dto.TomatoClockDto;
import com.todo.entity.TomatoClock;
import com.todo.service.TomatoClockService;
import com.todo.vo.Result;
import com.todo.vo.TomatoClockVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "番茄钟API")
@RestController
@RequestMapping("/clock")
public class TomatoClockController {

    private final TomatoClockService tomatoClockService;

    @Autowired
    public TomatoClockController(TomatoClockService tomatoClockService) {
        this.tomatoClockService = tomatoClockService;
    }

    @Operation(summary = "添加番茄钟")
    @PostMapping("/addTomatoClock/{taskId}/{estimate}")
    public Result<List<TomatoClockVo>> addTomatoClock(@PathVariable Long taskId, @PathVariable Integer estimate) {
        return tomatoClockService.addTomatoClock(taskId, estimate);
    }

    @Operation(summary = "开始执行一个番茄钟")
    @PutMapping("/startTomatoClock/{clockId}")
    public Result<?> startTomatoClock(@PathVariable Long clockId) {
        return tomatoClockService.startTomatoClock(clockId);
    }

    @Operation(summary = "完成一个番茄钟")
    @PutMapping("/completeTomatoClock/{clockId}")
    public Result<?> completeTomatoClock(@PathVariable Long clockId) {
        return tomatoClockService.completeTomatoClock(clockId);
    }

    @Operation(summary = "内部中断")
    @PutMapping("/innerInterrupt/{clockId}/{innerInterrupt}")
    public Result<?> innerInterrupt(@PathVariable Long clockId, @PathVariable Integer innerInterrupt) {
        return tomatoClockService.innerInterrupt(clockId, innerInterrupt);
    }

    @Operation(summary = "外部中断")
    @PutMapping("/outerInterrupt/{clockId}/{outerInterrupt}")
    public Result<?> outerInterrupt(@PathVariable Long clockId, @PathVariable Integer outerInterrupt) {
        return tomatoClockService.outerInterrupt(clockId, outerInterrupt);
    }

    @Operation(summary = "停止番茄钟")
    @PutMapping("/stopTomatoClock/{taskId}")
    public Result<?> stopTomatoClock(@PathVariable Long taskId, @RequestParam String stopReason) {
        return tomatoClockService.stopTomatoClock(taskId, stopReason);
    }

    @Operation(summary = "查询一个番茄钟")
    @GetMapping("/findTomatoClock/{clockId}")
    public Result<TomatoClockVo> findTomatoClock(@PathVariable Long clockId) {
        return tomatoClockService.findTomatoClock(clockId);
    }

    @Operation(summary = "查询一个任务的所有番茄钟")
    @GetMapping("/findTomatoClockAll/{taskId}")
    public Result<List<TomatoClockVo>> findTomatoClockAll(@PathVariable Long taskId) {
        return tomatoClockService.findTomatoClockAll(taskId);
    }

    @Operation(summary = "删除番茄钟")
    @DeleteMapping("/deleteTomatoClock/{taskId}")
    public Result<?> deleteTomatoClock(@PathVariable Long taskId) {
        return tomatoClockService.deleteTomatoClock(taskId);
    }

}
