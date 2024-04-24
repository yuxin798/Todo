package com.todo.exception;

import com.todo.vo.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
     * 参数捕获异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<?> argumentException(MethodArgumentNotValidException e) {
        String result = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(result);
    }

}
