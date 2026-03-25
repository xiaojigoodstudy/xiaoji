package com.xiaoji.toolkit.radar.model;

public class RadarTask {
    private final Long id;
    private final Long sourceId;
    private final String taskName;
    private final String cronExpression;
    private String status;
    private long lastRunAt;
    private long runCount;
    private final long createdAt;
    private long updatedAt;

    public RadarTask(Long id,
                     Long sourceId,
                     String taskName,
                     String cronExpression,
                     String status,
                     long createdAt,
                     long updatedAt) {
        this.id = id;
        this.sourceId = sourceId;
        this.taskName = taskName;
        this.cronExpression = cronExpression;
        this.status = status;
        this.lastRunAt = 0L;
        this.runCount = 0L;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(long lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public long getRunCount() {
        return runCount;
    }

    public void setRunCount(long runCount) {
        this.runCount = runCount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
