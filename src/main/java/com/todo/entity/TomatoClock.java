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
    @TableField(value = "sequence")
    private Integer sequence;

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

    public TomatoClock(Long taskId, int sequence, Integer clockDuration, Long parentId) {
        this.taskId = taskId;
        this.sequence = sequence;
        this.clockDuration = clockDuration;
        this.parentId = parentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TomatoClock that = (TomatoClock) o;
        return Objects.equals(clockId, that.clockId) && Objects.equals(taskId, that.taskId) && Objects.equals(parentId, that.parentId) && Objects.equals(sequence, that.sequence) && Objects.equals(clockDuration, that.clockDuration) && Objects.equals(clockStatus, that.clockStatus) && Objects.equals(stopReason, that.stopReason) && Objects.equals(startedAt, that.startedAt) && Objects.equals(completedAt, that.completedAt) && Objects.equals(createdAt, that.createdAt) && Objects.equals(updatedAt, that.updatedAt) && Objects.equals(deleted, that.deleted);
    }

    @Override
    public String toString() {
        return "TomatoClock{" +
                "clockId=" + clockId +
                ", taskId=" + taskId +
                ", parentId=" + parentId +
                ", sequence=" + sequence +
                ", clockDuration=" + clockDuration +
                ", clockStatus=" + clockStatus +
                ", stopReason='" + stopReason + '\'' +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deleted=" + deleted +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(clockId, taskId, parentId, sequence, clockDuration, clockStatus, stopReason, startedAt, completedAt, createdAt, updatedAt, deleted);
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