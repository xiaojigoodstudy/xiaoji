package com.xiaoji.toolkit.bootstrap.api;

import com.xiaoji.toolkit.notification.model.NotificationLog;

import java.util.List;

public class RadarRuleNotifyResponse {
    private final int matchedRuleCount;
    private final List<Long> matchedRuleIds;
    private final List<String> channels;
    private final List<NotificationLog> logs;

    public RadarRuleNotifyResponse(int matchedRuleCount,
                                   List<Long> matchedRuleIds,
                                   List<String> channels,
                                   List<NotificationLog> logs) {
        this.matchedRuleCount = matchedRuleCount;
        this.matchedRuleIds = matchedRuleIds;
        this.channels = channels;
        this.logs = logs;
    }

    public int getMatchedRuleCount() {
        return matchedRuleCount;
    }

    public List<Long> getMatchedRuleIds() {
        return matchedRuleIds;
    }

    public List<String> getChannels() {
        return channels;
    }

    public List<NotificationLog> getLogs() {
        return logs;
    }
}
