package com.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName tomato_clock
 */
@TableName(value ="tomato_clock")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TomatoClock implements Serializable {
    /**
     * 
     */
    @TableId(value = "clock_id", type = IdType.AUTO)
    private Long clockId;

    /**
     * 
     */
    @TableField(value = "task_id")
    private Long taskId;

    /**
     *
     */
    @TableField(value = "parent_id", javaType = true)
    private Long parentId;

    /**
     *
     */
    @TableField(value = "clock_duration")
    private Integer clockDuration;

    /**
     * 
     */
    @TableField(value = "clock_status")
    private Integer clockStatus;

    /**
     *
     */
    @TableField(value = "stop_reason")
    private String stopReason;

    /**
     *
     */
    @TableField(value = "user_id")
    private Long userId;

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

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public TomatoClock(Long taskId, Integer clockDuration, Long parentId, Long userId) {
        this.taskId = taskId;
        this.clockDuration = clockDuration;
        this.parentId = parentId;
        this.userId = userId;
    }

    public TomatoClock(Long taskId, Long parentId, Integer clockDuration, Integer clockStatus) {
        this.taskId = taskId;
        this.parentId = parentId;
        this.clockDuration = clockDuration;
        this.clockStatus = clockStatus;
    }

    @Override
    public String toString() {
        return "TomatoClock{" +
                "clockId=" + clockId +
                ", taskId=" + taskId +
                ", parentId=" + parentId +
                ", clockDuration=" + clockDuration +
                ", clockStatus=" + clockStatus +
                ", stopReason='" + stopReason + '\'' +
                ", userId=" + userId +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deleted=" + deleted +
                '}';
    }

    public TomatoClock.Status clockStatusEnum() {
        return TomatoClock.Status.of(this.clockStatus);
    }

    public void clockStatusEnum(TomatoClock.Status clockStatus) {
        this.clockStatus = clockStatus.code;
    }

    @Getter
    public enum Status {
        COMPLETED(0, "已完成"), DOING(1, "正在进行"), NOT_STARTED(2, "未开始"), TERMINATED(3, "已终止");

        private final Integer code;
        private final String info;

        Status(Integer code, String info) {
            this.code = code;
            this.info = info;
        }

        public static TomatoClock.Status of(Integer code) {
            return switch (code) {
                case 0 -> TomatoClock.Status.COMPLETED;
                case 1 -> TomatoClock.Status.DOING;
                case 2 -> TomatoClock.Status.NOT_STARTED;
                case 3 -> TomatoClock.Status.TERMINATED;
                default -> null;
            };
        }
    }
}