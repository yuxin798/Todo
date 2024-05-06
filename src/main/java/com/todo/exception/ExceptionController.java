package com.todo.exception;

import com.todo.vo.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Objects;

@RestControllerAdvice
public class ExceptionController {

    /**
     * 全局捕获异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> allExceptionHandler(Exception e){
        e.printStackTrace();
        return Result.error(e.getMessage());
    }

    /**
     * 参数捕获异常--写在实体属性上
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<?> argumentException(MethodArgumentNotValidException  e) {
        e.printStackTrace();
        return Result.error(Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());
    }

    /**
     * 参数捕获异常--写在请求参数上
     */
    @ExceptionHandler(value = HandlerMethodValidationException.class)
    public Result<?> handlerMethodValidationException(HandlerMethodValidationException  e) {
        e.printStackTrace();
        return Result.error(e.getAllErrors().get(0).getDefaultMessage());
    }
}
