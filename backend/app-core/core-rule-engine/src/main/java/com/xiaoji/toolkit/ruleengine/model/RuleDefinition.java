package com.xiaoji.toolkit.ruleengine.model;

public class RuleDefinition {
    private final Long id;
    private final String ruleName;
    private final String keyword;
    private final String sourceType;
    private final String notifyChannel;
    private boolean enabled;
    private final long createdAt;
    private long updatedAt;

    public RuleDefinition(Long id,
                          String ruleName,
                          String keyword,
                          String sourceType,
                          String notifyChannel,
                          boolean enabled,
                          long createdAt,
                          long updatedAt) {
        this.id = id;
        this.ruleName = ruleName;
        this.keyword = keyword;
        this.sourceType = sourceType;
        this.notifyChannel = notifyChannel;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getNotifyChannel() {
        return notifyChannel;
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
