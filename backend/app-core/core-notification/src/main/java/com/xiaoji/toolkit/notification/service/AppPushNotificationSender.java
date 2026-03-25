package com.xiaoji.toolkit.notification.service;

import org.springframework.stereotype.Component;

@Component
public class AppPushNotificationSender implements NotificationChannelSender {
    @Override
    public String channelCode() {
        return "APP_PUSH";
    }

    @Override
    public String send(String title, String content, String target) {
        return "app push sent to " + target;
    }
}
