package com.xiaoji.toolkit.radar.model;

public class RadarSource {
    private final Long id;
    private final String name;
    private final String sourceType;
    private final String sourceUrl;
    private boolean enabled;
    private final long createdAt;
    private long updatedAt;

    public RadarSource(Long id, String name, String sourceType, String sourceUrl, boolean enabled, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.sourceType = sourceType;
        this.sourceUrl = sourceUrl;
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

    public String getSourceType() {
        return sourceType;
    }

    public String getSourceUrl() {
        return sourceUrl;
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
