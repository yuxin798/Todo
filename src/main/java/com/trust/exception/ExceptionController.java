package com.trust.exception;

import com.trust.util.ResultVOUtil;
import com.trust.vo.Result;
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
