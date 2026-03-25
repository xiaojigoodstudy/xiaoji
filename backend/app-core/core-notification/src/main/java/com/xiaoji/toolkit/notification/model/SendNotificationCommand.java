package com.xiaoji.toolkit.notification.model;

import java.util.ArrayList;
import java.util.List;

public class SendNotificationCommand {
    private final String title;
    private final String content;
    private final String target;
    private final List<String> channels;

    public SendNotificationCommand(String title, String content, String target, List<String> channels) {
        this.title = title;
        this.content = content;
        this.target = target;
        this.channels = channels == null ? new ArrayList<String>() : channels;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTarget() {
        return target;
    }

    public List<String> getChannels() {
        return channels;
    }
}
