package com.xiaoji.toolkit.habit.model;

public class HabitCheckinRecord {
    private final Long id;
    private final Long habitItemId;
    private final Long userId;
    private final String checkinDate;
    private final long createdAt;

    public HabitCheckinRecord(Long id, Long habitItemId, Long userId, String checkinDate, long createdAt) {
        this.id = id;
        this.habitItemId = habitItemId;
        this.userId = userId;
        this.checkinDate = checkinDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getHabitItemId() {
        return habitItemId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCheckinDate() {
        return checkinDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
