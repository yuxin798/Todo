package com.todo.exception;

import com.todo.vo.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ExceptionController {

    /**
     * 全局捕获异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result<?> allExceptionHandler(Exception e){
        e.printStackTrace();
        return Result.error(e.getMessage());
    }

    /**
     * 参数捕获异常--写在实体属性上
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<?> argumentException(MethodArgumentNotValidException  e) {
        return Result.error(e.getBindingResult().getFieldError().getDefaultMessage());
    }

    /**
     * 参数捕获异常--写在请求参数上
     * @param e
     * @return
     */
    @ExceptionHandler(value = HandlerMethodValidationException.class)
    public Result<?> handlerMethodValidationException(HandlerMethodValidationException  e) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return Result.error(message);
    }

}
