package com.todo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName tomato_clock
 */
@TableName(value ="tomato_clock")
@Data
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
    @TableField(value = "sequence")
    private Integer sequence;

    /**
     * 
     */
    @TableField(value = "task_status")
    private Integer taskStatus;

    /**
     * 
     */
    @TableField(value = "stop_reason")
    private String stopReason;

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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TomatoClock other = (TomatoClock) that;
        return (this.getClockId() == null ? other.getClockId() == null : this.getClockId().equals(other.getClockId()))
            && (this.getTaskId() == null ? other.getTaskId() == null : this.getTaskId().equals(other.getTaskId()))
            && (this.getSequence() == null ? other.getSequence() == null : this.getSequence().equals(other.getSequence()))
            && (this.getTaskStatus() == null ? other.getTaskStatus() == null : this.getTaskStatus().equals(other.getTaskStatus()))
            && (this.getStopReason() == null ? other.getStopReason() == null : this.getStopReason().equals(other.getStopReason()))
            && (this.getInnerInterrupt() == null ? other.getInnerInterrupt() == null : this.getInnerInterrupt().equals(other.getInnerInterrupt()))
            && (this.getOuterInterrupt() == null ? other.getOuterInterrupt() == null : this.getOuterInterrupt().equals(other.getOuterInterrupt()))
            && (this.getStartedAt() == null ? other.getStartedAt() == null : this.getStartedAt().equals(other.getStartedAt()))
            && (this.getCompletedAt() == null ? other.getCompletedAt() == null : this.getCompletedAt().equals(other.getCompletedAt()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getDeleted() == null ? other.getDeleted() == null : this.getDeleted().equals(other.getDeleted()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getClockId() == null) ? 0 : getClockId().hashCode());
        result = prime * result + ((getTaskId() == null) ? 0 : getTaskId().hashCode());
        result = prime * result + ((getSequence() == null) ? 0 : getSequence().hashCode());
        result = prime * result + ((getTaskStatus() == null) ? 0 : getTaskStatus().hashCode());
        result = prime * result + ((getStopReason() == null) ? 0 : getStopReason().hashCode());
        result = prime * result + ((getInnerInterrupt() == null) ? 0 : getInnerInterrupt().hashCode());
        result = prime * result + ((getOuterInterrupt() == null) ? 0 : getOuterInterrupt().hashCode());
        result = prime * result + ((getStartedAt() == null) ? 0 : getStartedAt().hashCode());
        result = prime * result + ((getCompletedAt() == null) ? 0 : getCompletedAt().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getDeleted() == null) ? 0 : getDeleted().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", clockId=").append(clockId);
        sb.append(", taskId=").append(taskId);
        sb.append(", sequence=").append(sequence);
        sb.append(", taskStatus=").append(taskStatus);
        sb.append(", stopReason=").append(stopReason);
        sb.append(", innerInterrupt=").append(innerInterrupt);
        sb.append(", outerInterrupt=").append(outerInterrupt);
        sb.append(", startedAt=").append(startedAt);
        sb.append(", completedAt=").append(completedAt);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", deleted=").append(deleted);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}