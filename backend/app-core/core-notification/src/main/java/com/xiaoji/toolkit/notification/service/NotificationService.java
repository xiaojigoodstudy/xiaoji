package com.xiaoji.toolkit.notification.service;

import com.xiaoji.toolkit.notification.model.NotificationLog;
import com.xiaoji.toolkit.notification.model.SendNotificationCommand;

import java.util.List;

public interface NotificationService {
    List<NotificationLog> send(SendNotificationCommand command);

    List<NotificationLog> listLogs();
}
