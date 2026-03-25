package com.xiaoji.toolkit.notification.service;

import com.xiaoji.toolkit.notification.model.NotificationLog;
import com.xiaoji.toolkit.notification.model.SendNotificationCommand;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryNotificationService implements NotificationService {

    private final AtomicLong logIdGen = new AtomicLong(5000L);
    private final CopyOnWriteArrayList<NotificationLog> logStore = new CopyOnWriteArrayList<NotificationLog>();
    private final Map<String, NotificationChannelSender> senderByCode = new ConcurrentHashMap<String, NotificationChannelSender>();

    public InMemoryNotificationService(List<NotificationChannelSender> senders) {
        if (senders != null) {
            for (NotificationChannelSender sender : senders) {
                senderByCode.put(sender.channelCode().toUpperCase(Locale.ROOT), sender);
            }
        }
    }

    @Override
    public List<NotificationLog> send(SendNotificationCommand command) {
        validate(command);
        List<NotificationLog> result = new ArrayList<NotificationLog>();

        for (String channel : command.getChannels()) {
            String key = channel == null ? "" : channel.trim().toUpperCase(Locale.ROOT);
            NotificationChannelSender sender = senderByCode.get(key);
            if (sender == null) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "unsupported channel: " + channel);
            }

            String detail = sender.send(command.getTitle(), command.getContent(), command.getTarget());
            NotificationLog log = new NotificationLog(
                    logIdGen.incrementAndGet(),
                    key,
                    command.getTarget(),
                    command.getTitle(),
                    command.getContent(),
                    "SUCCESS",
                    detail,
                    System.currentTimeMillis()
            );
            logStore.add(log);
            result.add(log);
        }

        return result;
    }

    @Override
    public List<NotificationLog> listLogs() {
        ArrayList<NotificationLog> copy = new ArrayList<NotificationLog>(logStore);
        Collections.reverse(copy);
        return copy;
    }

    private void validate(SendNotificationCommand command) {
        if (command == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "command is null");
        }
        if (isBlank(command.getTitle()) || isBlank(command.getContent()) || isBlank(command.getTarget())) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "title/content/target is required");
        }
        if (command.getChannels() == null || command.getChannels().isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "channels is required");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
