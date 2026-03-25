package com.xiaoji.toolkit.radar.service;

import com.xiaoji.toolkit.radar.model.RadarSource;
import com.xiaoji.toolkit.radar.model.RadarTask;

import java.util.List;

public interface RadarService {
    RadarSource createSource(String name, String sourceType, String sourceUrl, boolean enabled);

    List<RadarSource> listSources();

    RadarSource setSourceEnabled(Long sourceId, boolean enabled);

    RadarTask createTask(Long sourceId, String taskName, String cronExpression);

    List<RadarTask> listTasks();

    RadarTask runTask(Long taskId);
}
