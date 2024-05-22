package com.todo.controller;

import com.todo.service.RandomWordService;
import com.todo.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "随机一言API")
@RestController
@RequestMapping("/word")
public class RandomWordController {

    private final RandomWordService randomWordService;

    public RandomWordController(RandomWordService randomWordService) {
        this.randomWordService = randomWordService;
    }

    /*
     * 新增随机一言
     */
    @Operation(summary = "新增随机一言")
    @GetMapping("/insertRandomWord")
    public Result<String> insertRandomWord(Integer times, Integer length){
        return randomWordService.insertRandomWord(times, length);
    }

    /*
     * 获取随机一言
     */
    @Operation(summary = "获取随机一言")
    @GetMapping("/getRandomWord")
    public Result<String> getRandomWord(){
        return randomWordService.getRandomWord();
    }
}
