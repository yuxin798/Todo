package com.todo.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.todo.entity.Task;
import com.todo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.Date;
import java.util.List;

@Component
public class TaskSchedule {

    private final TaskService taskService;

    @Autowired
    public TaskSchedule(TaskService taskService) {
        this.taskService = taskService;
    }

//    @Scheduled(cron = "0 */1 * * * ?") 每分钟执行一次  用于测试

    // 0 0 2 * * ?  每天2点执行一次
    @Scheduled(cron = "0 30 23 * * ?")
    public void taskScheduleInsert() {
        long now = System.currentTimeMillis();
        LocalDate date = LocalDate.ofInstant(Instant.ofEpochMilli(now), ZoneId.of("UTC+8"));
        Date start = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MIN, ZoneOffset.of("+8"))));
        Date end = Date.from(Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MAX, ZoneOffset.of("+8"))));

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>(Task.class)
                .ge(Task::getCreatedAt, start)
                .le(Task::getCreatedAt, end)
                .eq(Task::getAgain, 0);

        List<Task> tasks = taskService.list(wrapper)
                .stream()
                .peek(t -> {
                    if (t.getCategoryId() == null){
                        t.setTaskStatus(0);
                    }else {
                        t.setTaskStatus(1);
                    }
                    t.setTaskId(null);
                    t.setCreatedAt(new Date(t.getCreatedAt().getTime() + 3600 * 24 * 1000));
                    t.setStartedAt(null);
                    t.setCompletedAt(null);
                    t.setUpdatedAt(null);
                })
                .toList();
        taskService.saveBatch(tasks);
    }
}
