package com.xiaoji.toolkit.radar.service;

import com.xiaoji.toolkit.radar.model.RadarSource;
import com.xiaoji.toolkit.radar.model.RadarTask;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("!mysql")
public class InMemoryRadarService implements RadarService {

    private final AtomicLong sourceIdGen = new AtomicLong(1000L);
    private final AtomicLong taskIdGen = new AtomicLong(2000L);

    private final ConcurrentHashMap<Long, RadarSource> sourceStore = new ConcurrentHashMap<Long, RadarSource>();
    private final ConcurrentHashMap<Long, RadarTask> taskStore = new ConcurrentHashMap<Long, RadarTask>();

    @Override
    public RadarSource createSource(String name, String sourceType, String sourceUrl, boolean enabled) {
        if (isBlank(name) || isBlank(sourceType) || isBlank(sourceUrl)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "name/sourceType/sourceUrl is required");
        }

        long now = System.currentTimeMillis();
        Long id = sourceIdGen.incrementAndGet();
        RadarSource source = new RadarSource(id, name.trim(), sourceType.trim(), sourceUrl.trim(), enabled, now, now);
        sourceStore.put(id, source);
        return source;
    }

    @Override
    public List<RadarSource> listSources() {
        return new ArrayList<RadarSource>(sourceStore.values());
    }

    @Override
    public RadarSource setSourceEnabled(Long sourceId, boolean enabled) {
        RadarSource source = sourceStore.get(sourceId);
        if (source == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "source not found: " + sourceId);
        }
        source.setEnabled(enabled);
        source.setUpdatedAt(System.currentTimeMillis());
        return source;
    }

    @Override
    public RadarTask createTask(Long sourceId, String taskName, String cronExpression) {
        if (sourceId == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "sourceId is required");
        }
        if (!sourceStore.containsKey(sourceId)) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "source not found: " + sourceId);
        }
        if (isBlank(taskName) || isBlank(cronExpression)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "taskName/cronExpression is required");
        }

        long now = System.currentTimeMillis();
        Long id = taskIdGen.incrementAndGet();
        RadarTask task = new RadarTask(id, sourceId, taskName.trim(), cronExpression.trim(), "CREATED", now, now);
        taskStore.put(id, task);
        return task;
    }

    @Override
    public List<RadarTask> listTasks() {
        return new ArrayList<RadarTask>(taskStore.values());
    }

    @Override
    public RadarTask runTask(Long taskId) {
        RadarTask task = taskStore.get(taskId);
        if (task == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "task not found: " + taskId);
        }

        RadarSource source = sourceStore.get(task.getSourceId());
        if (source == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "source not found: " + task.getSourceId());
        }
        if (!source.isEnabled()) {
            throw new BizException(ResultCode.BIZ_ERROR.getCode(), "source is disabled: " + source.getId());
        }

        task.setStatus("SUCCESS");
        task.setLastRunAt(System.currentTimeMillis());
        task.setRunCount(task.getRunCount() + 1L);
        task.setUpdatedAt(System.currentTimeMillis());
        return task;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
