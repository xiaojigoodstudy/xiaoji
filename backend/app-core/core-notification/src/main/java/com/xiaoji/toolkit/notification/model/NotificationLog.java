package com.xiaoji.toolkit.notification.model;

public class NotificationLog {
    private final Long id;
    private final String channel;
    private final String target;
    private final String title;
    private final String content;
    private final String status;
    private final String detail;
    private final long createdAt;

    public NotificationLog(Long id,
                           String channel,
                           String target,
                           String title,
                           String content,
                           String status,
                           String detail,
                           long createdAt) {
        this.id = id;
        this.channel = channel;
        this.target = target;
        this.title = title;
        this.content = content;
        this.status = status;
        this.detail = detail;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getChannel() {
        return channel;
    }

    public String getTarget() {
        return target;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
