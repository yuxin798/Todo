package com.todo;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;

public class DateTest {
    @Test
    void dateTest() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        System.out.println(new SimpleDateFormat().format(calendar.getTime()));
    }

    @Test
    void timestampTest() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        // 从今天起 往上推一天
        LocalDateTime yesterday = currentDateTime.minusDays(1);
        // 从今天起 往上推一周
        LocalDateTime week = currentDateTime.minusWeeks(1).plusDays(1);
        // 从今天起 往上推一个月
        LocalDateTime month = currentDateTime.minusDays(29);

        long yesterdayEpochMilli = yesterday.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long weekEpochMilli = week.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long monthEpochMilli = month.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        System.out.println("从今天起 往上推一天 " + yesterdayEpochMilli);
        System.out.println("从今天起 往上推一周 " + weekEpochMilli);
        System.out.println("从今天起 往上推一个月 " + monthEpochMilli);
    }

    @Test
    void LocalDateTest() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Year" + "--" + now.getYear());
        System.out.println("Month" + "--" + now.getMonth());
        System.out.println("DayOfYear" + "--" + now.getDayOfYear());
        System.out.println("DayOfWeek" + "--" + now.getDayOfWeek());
        System.out.println("DayOfMonth" + "--" + now.getDayOfMonth());
        System.out.println("Hour" + "--" + now.getHour());
        System.out.println("Minute" + "--" + now.getMinute());
        System.out.println("MonthValue" + "--" + now.getMonthValue());
        System.out.println("Second" + "--" + now.getSecond());
        System.out.println("Nano" + "--" + now.getNano());
    }

    @Test
    void MonthDayTest() {
        System.out.println(MonthDay.now());
        System.out.println(MonthDay.now(ZoneId.of("+8")));
        System.out.println(MonthDay.of(Month.MAY, 15));
        System.out.println(MonthDay.parse("5--25", DateTimeFormatter.ofPattern("M--dd")));
    }

    @Test
    void ClockTest() {
        System.out.println(Clock.systemUTC().millis());
        System.out.println(Clock.system(ZoneId.of("+8")).millis());
        System.out.println(Clock.systemDefaultZone().millis());
        System.out.println(Clock.fixed(Instant.now(), ZoneId.of("+8")).millis());
    }

    @Test
    void generator() {
        for (int i = 0; i < 100; i++) {
            System.out.println(DefaultIdentifierGenerator.getInstance().nextId(null));
        }
    }

    @Test
    void LocalDateTimeTest() {
        LocalDateTime now = LocalDateTime.now();
        // 本月第一天0时0分
        LocalDateTime firstDay = LocalDateTime.of(LocalDate.from(now.with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        // 本月最后一天23：59：59
        LocalDateTime lastDay = LocalDateTime.of(LocalDate.from(now.with(TemporalAdjusters.lastDayOfMonth())), LocalTime.MAX);

        System.out.println("本月第一天0时0分" + firstDay);
        System.out.println("本月最后一天23：59：59" + lastDay);
    }

    @Test
    void Calendar() {
        // 每周 周一
//        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
//        calendar.setTimeInMillis(1716619487000L);
//        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        System.out.println(calendar.getTime().toInstant().toEpochMilli());

        // 每月 1号
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1716619487000L);
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        System.out.println(calendar.getTime().toInstant().toEpochMilli());
    }

    @Test
    void name() {
//        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1714290053000L);
//        // 设置第一个Calendar为本周周一
//        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//        Calendar mondayCalendar = (Calendar) calendar.clone();
//        System.out.println("本周周一：" + mondayCalendar.getTime());
//
//        // 设置第二个Calendar为本周周日
//        calendar.add(Calendar.DATE, 6); // 加6天到下周周一
//        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
//        Calendar sundayCalendar = (Calendar) calendar.clone();
//        System.out.println("本周周日：" + sundayCalendar.getTime());
        System.out.println(calendar.get(Calendar.DAY_OF_WEEK));
    }
}
