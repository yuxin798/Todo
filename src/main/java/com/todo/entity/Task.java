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
 * @TableName task
 */
@TableName(value ="task")
@Data
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
    @TableField(value = "estimate")
    private String estimate;

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
        Task other = (Task) that;
        return (this.getTaskId() == null ? other.getTaskId() == null : this.getTaskId().equals(other.getTaskId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTaskName() == null ? other.getTaskName() == null : this.getTaskName().equals(other.getTaskName()))
            && (this.getEstimate() == null ? other.getEstimate() == null : this.getEstimate().equals(other.getEstimate()))
            && (this.getTomatoClockTimes() == null ? other.getTomatoClockTimes() == null : this.getTomatoClockTimes().equals(other.getTomatoClockTimes()))
            && (this.getStopTimes() == null ? other.getStopTimes() == null : this.getStopTimes().equals(other.getStopTimes()))
            && (this.getTaskStatus() == null ? other.getTaskStatus() == null : this.getTaskStatus().equals(other.getTaskStatus()))
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
        result = prime * result + ((getTaskId() == null) ? 0 : getTaskId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTaskName() == null) ? 0 : getTaskName().hashCode());
        result = prime * result + ((getEstimate() == null) ? 0 : getEstimate().hashCode());
        result = prime * result + ((getTomatoClockTimes() == null) ? 0 : getTomatoClockTimes().hashCode());
        result = prime * result + ((getStopTimes() == null) ? 0 : getStopTimes().hashCode());
        result = prime * result + ((getTaskStatus() == null) ? 0 : getTaskStatus().hashCode());
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
        sb.append(", taskId=").append(taskId);
        sb.append(", userId=").append(userId);
        sb.append(", taskName=").append(taskName);
        sb.append(", estimate=").append(estimate);
        sb.append(", tomatoClockTimes=").append(tomatoClockTimes);
        sb.append(", stopTimes=").append(stopTimes);
        sb.append(", taskStatus=").append(taskStatus);
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