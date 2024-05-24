package com.todo.schedule;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.todo.entity.Task;
import com.todo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskSchedule {

    private final TaskService taskService;

    @Autowired
    public TaskSchedule(TaskService taskService) {
        this.taskService = taskService;
    }

//    @Scheduled(cron = "0 */1 * * * ?") 每分钟执行一次  用于测试

    // 0 0 0 * * ?  每天0点执行一次
    @Scheduled(cron = "0 0 0 * * ?")
    public void taskSchedule() {
        // 重复任务 再次添加到 今日待办
        taskService.update(new LambdaUpdateWrapper<Task>()
                .set(Task::getTaskStatus, 0)
                .set(Task::getTodayTotalTimes, 0)
                .eq(Task::getAgain, 0)
                .in(Task::getTaskStatus, 2, 3)
                .isNull(Task::getCategoryId));

        //
        taskService.update(new LambdaUpdateWrapper<Task>()
                .set(Task::getTaskStatus, 1)
                .set(Task::getTodayTotalTimes, 0)
                .eq(Task::getAgain, 0)
                .in(Task::getTaskStatus, 2, 3)
                .isNotNull(Task::getCategoryId));
    }
}
