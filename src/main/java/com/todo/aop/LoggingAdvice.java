package com.todo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class LoggingAdvice {
    @Around("execution(public * com.todo.controller.*.*(..))")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        try {
            // 目标方法的执行，目标方法的返回值一定要返回给外界调用者
            result = joinPoint.proceed();
            logTemplate(joinPoint, result);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return result;
    }

    private void logTemplate(ProceedingJoinPoint joinPoint, Object result) {
        log.debug(
                """
                
                
                ==> Method:
                {}
                
                ==> Parameters:
                {}
                
                ==> Return:
                {}
                
                """,
                logName(joinPoint),
                logArgs(joinPoint),
                logResult(result));
    }

    private Object logArgs(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            stringBuilder
                    .append(args[i].getClass().getName())
                    .append(" = ")
                    .append(args[i])
                    .append(",")
                    .append("\n");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private Object logName(ProceedingJoinPoint joinPoint) {
        return joinPoint.getSignature().toLongString();
    }

    private String logResult(Object result) {
        StringBuilder stringBuilder = new StringBuilder();
        if (result instanceof Collection<?> collection) {
            collection.forEach(v -> {
                stringBuilder
                        .append(v.toString())
                        .append("\n");
            });

        } else if (result instanceof Object[] array) {
            Arrays.stream(array).forEach(v -> {
                stringBuilder
                        .append(v.toString())
                        .append("\n");
            });

        } else if (result instanceof Map) {
            ((Map<?, ?>) result).forEach((k, v) -> {
                stringBuilder
                        .append(k.toString())
                        .append(": ")
                        .append(v.toString())
                        .append("\n");
            });

        } else {
            stringBuilder
                    .append(result)
                    .append("\n");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
}
