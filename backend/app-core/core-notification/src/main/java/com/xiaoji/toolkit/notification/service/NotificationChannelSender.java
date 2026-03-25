package com.xiaoji.toolkit.notification.service;

public interface NotificationChannelSender {
    String channelCode();

    String send(String title, String content, String target);
}
