package com.todo;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

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
}
