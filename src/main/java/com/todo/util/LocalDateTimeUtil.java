package com.todo.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class LocalDateTimeUtil {
    public static LocalDateTime of(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC+8"));
    }
}
