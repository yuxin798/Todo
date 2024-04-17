package com.todo.exception;

import com.todo.util.ResultVOUtil;
import com.todo.vo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(Exception.class)
    public Result<?> allExceptionHandler(Exception e){
        e.getStackTrace();
        return ResultVOUtil.error(e.getMessage());
    }
}
