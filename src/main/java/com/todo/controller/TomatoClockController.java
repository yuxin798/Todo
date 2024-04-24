package com.todo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "番茄钟API")
@RestController
@RequestMapping("/clock")
public class TomatoClockController {

}
