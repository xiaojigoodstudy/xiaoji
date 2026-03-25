package com.xiaoji.toolkit.notification.service;

import org.springframework.stereotype.Component;

@Component
public class WechatNotificationSender implements NotificationChannelSender {
    @Override
    public String channelCode() {
        return "WECHAT";
    }

    @Override
    public String send(String title, String content, String target) {
        return "wechat sent to " + target;
    }
}
