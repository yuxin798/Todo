package com.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 
 * @TableName task
 */
@TableName(value ="task")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task implements Serializable {
    /**
     * 
     */
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    /**
     * 
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 
     */
    @TableField(value = "task_name")
    private String taskName;

    /**
     *
     */
    @TableField(value = "type")
    private Integer type;

    /**
     *
     */
    @TableField(value = "clock_duration")
    private Integer clockDuration;

    /**
     *
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 
     */
    @TableField(value = "estimate")
    private String estimate;

    /**
     *
     */
    @TableField(value = "rest_time")
    private Integer restTime;

    /**
     *
     */
    @TableField(value = "again")
    private Integer again;

    /**
     *
     */
    @TableField(value = "category")
    private String category;

    /**
     *
     */
    @TableField(value = "today_total_times")
    private Integer todayTotalTimes;

    /**
     * 
     */
    @TableField(value = "tomato_clock_times")
    private Integer tomatoClockTimes;

    /**
     * 
     */
    @TableField(value = "stop_times")
    private Integer stopTimes;

    /**
     * 
     */
    @TableField(value = "task_status")
    private Integer taskStatus;

    /**
     *
     */
    @TableField(value = "background")
    private String background;

    /**
     * 
     */
    @TableField(value = "inner_interrupt")
    private Integer innerInterrupt;

    /**
     * 
     */
    @TableField(value = "outer_interrupt")
    private Integer outerInterrupt;

    /**
     * 
     */
    @TableField(value = "started_at")
    private Date startedAt;

    /**
     * 
     */
    @TableField(value = "completed_at")
    private Date completedAt;

    /**
     * 
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 
     */
    @TableField(value = "updated_at")
    private Date updatedAt;

    /**
     * 
     */
    @TableField(value = "deleted")
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public Task(Long userId, String taskName, Integer type, String estimate, Integer clockDuration, String background, String remark, Integer restTime, Integer again) {
        this.userId = userId;
        this.taskName = taskName;
        this.type = type;
        this.estimate = estimate;
        this.clockDuration = clockDuration;
        this.background = background;
        this.remark = remark;
        this.again = again;
        this.restTime = restTime;

        this.setTomatoClockTimes(0);
        this.setStopTimes(0);
        this.taskStatusEnum(Status.CHECKLIST);
        this.setInnerInterrupt(0);
        this.setOuterInterrupt(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(taskId, task.taskId) && Objects.equals(userId, task.userId) && Objects.equals(taskName, task.taskName) && Objects.equals(type, task.type) && Objects.equals(clockDuration, task.clockDuration) && Objects.equals(remark, task.remark) && Objects.equals(estimate, task.estimate) && Objects.equals(restTime, task.restTime) && Objects.equals(again, task.again) && Objects.equals(category, task.category) && Objects.equals(todayTotalTimes, task.todayTotalTimes) && Objects.equals(tomatoClockTimes, task.tomatoClockTimes) && Objects.equals(stopTimes, task.stopTimes) && Objects.equals(taskStatus, task.taskStatus) && Objects.equals(background, task.background) && Objects.equals(innerInterrupt, task.innerInterrupt) && Objects.equals(outerInterrupt, task.outerInterrupt) && Objects.equals(startedAt, task.startedAt) && Objects.equals(completedAt, task.completedAt) && Objects.equals(createdAt, task.createdAt) && Objects.equals(updatedAt, task.updatedAt) && Objects.equals(deleted, task.deleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, userId, taskName, type, clockDuration, remark, estimate, restTime, again, category, todayTotalTimes, tomatoClockTimes, stopTimes, taskStatus, background, innerInterrupt, outerInterrupt, startedAt, completedAt, createdAt, updatedAt, deleted);
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", userId=" + userId +
                ", taskName='" + taskName + '\'' +
                ", type=" + type +
                ", clockDuration=" + clockDuration +
                ", remark='" + remark + '\'' +
                ", estimate='" + estimate + '\'' +
                ", restTime=" + restTime +
                ", again=" + again +
                ", category='" + category + '\'' +
                ", todayTotalTimes=" + todayTotalTimes +
                ", tomatoClockTimes=" + tomatoClockTimes +
                ", stopTimes=" + stopTimes +
                ", taskStatus=" + taskStatus +
                ", background='" + background + '\'' +
                ", innerInterrupt=" + innerInterrupt +
                ", outerInterrupt=" + outerInterrupt +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deleted=" + deleted +
                '}';
    }

    public Task.Status taskStatusEnum() {
        return Status.of(taskStatus);
    }

    public void taskStatusEnum(Task.Status taskStatus) {
        this.taskStatus = taskStatus.code;
    }

    @Getter
    public enum Status {
        TODO_TODAY(0, "今日待办"), CHECKLIST(1, "清单列表"), COMPLETED(2, "已完成"), DELETED(3, "已删除");

        private final Integer code;
        private final String info;

        Status(Integer code, String info) {
            this.code = code;
            this.info = info;
        }

        public static Status of(Integer code) {
            return switch (code) {
                case 0 -> Task.Status.TODO_TODAY;
                case 1 -> Task.Status.CHECKLIST;
                case 2 -> Task.Status.COMPLETED;
                case 3 -> Task.Status.DELETED;
                default -> null;
            };
        }
    }
}