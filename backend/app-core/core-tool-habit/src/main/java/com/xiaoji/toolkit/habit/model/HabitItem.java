package com.xiaoji.toolkit.habit.model;

public class HabitItem {
    private final Long id;
    private final String name;
    private boolean enabled;
    private final long createdAt;
    private long updatedAt;

    public HabitItem(Long id, String name, boolean enabled, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
