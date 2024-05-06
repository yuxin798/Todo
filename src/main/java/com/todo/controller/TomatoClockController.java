package com.todo.controller;

import com.todo.dto.TomatoClockDto;
import com.todo.entity.TomatoClock;
import com.todo.service.TomatoClockService;
import com.todo.vo.Result;
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

    private TomatoClockService tomatoClockService;

    @Autowired
    public TomatoClockController(TomatoClockService tomatoClockService) {
        this.tomatoClockService = tomatoClockService;
    }

    @Operation(summary = "添加番茄钟")
    @PostMapping("/addTomatoClock")
    public Result<?> addTomatoClock(@NotNull(message = "番茄钟不能为空") @RequestBody List<TomatoClock> tomatoClockList) {
        return tomatoClockService.addTomatoClock(tomatoClockList);
    }

    @Operation(summary = "完成一个番茄钟")
    @GetMapping("/completeTomatoClock")
    public Result<?> completeTomatoClock(@NotNull(message = "番茄钟ID不能为空") Long clockId) {
        return tomatoClockService.completeTomatoClock(clockId);
    }

    @Operation(summary = "内部中断")
    @GetMapping("/innerInterrupt/{clockId}/{innerInterrupt}")
    public Result<?> innerInterrupt(@NotNull(message = "番茄钟Id不能为空") @PathVariable Long clockId, @NotNull(message = "内部中断数不能为空") @PathVariable Integer innerInterrupt) {
        return tomatoClockService.innerInterrupt(clockId, innerInterrupt);
    }

    @Operation(summary = "外部中断")
    @GetMapping("/outerInterrupt/{clockId}/{outerInterrupt}")
    public Result<?> outerInterrupt(@NotNull(message = "番茄钟Id不能为空") @PathVariable Long clockId, @NotNull(message = "外部中断数不能为空") @PathVariable Integer outerInterrupt) {
        return tomatoClockService.outerInterrupt(clockId, outerInterrupt);
    }

}
