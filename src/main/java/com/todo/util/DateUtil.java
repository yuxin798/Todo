package com.todo.util;

import java.time.*;
import java.util.Date;

public class DateUtil {

    public static Date todayMinTime(){
        LocalDate todayDate = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC+8"));
        return Date.from(Instant.ofEpochSecond(todayDate.toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"))));
    }

    public static Date todayMaxTime(){
        LocalDate todayDate = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC+8"));
        return Date.from(Instant.ofEpochSecond(todayDate.toEpochSecond(LocalTime.MAX, ZoneOffset.of("+8"))));
    }

    public static Date tomorrowMinTime(){
        LocalDate todayDate = LocalDate.ofInstant(Instant.now(), ZoneId.of("UTC+8"));
        return Date.from(Instant.ofEpochSecond(todayDate.plusDays(1).toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"))));
    }
}
