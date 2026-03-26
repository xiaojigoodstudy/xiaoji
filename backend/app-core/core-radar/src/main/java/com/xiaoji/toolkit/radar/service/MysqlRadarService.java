package com.xiaoji.toolkit.radar.service;

import com.xiaoji.toolkit.radar.model.RadarSource;
import com.xiaoji.toolkit.radar.model.RadarTask;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Service
@Profile("mysql")
public class MysqlRadarService implements RadarService {

    private static final String SQL_INSERT_SOURCE =
            "INSERT INTO radar_source " +
                    "(id, name, source_type, source_url, enabled, last_fetch_at, created_at, updated_at, is_deleted) " +
                    "VALUES (?, ?, ?, ?, ?, NULL, ?, ?, 0)";

    private static final String SQL_LIST_SOURCE =
            "SELECT id, name, source_type, source_url, enabled, created_at, updated_at " +
                    "FROM radar_source " +
                    "WHERE is_deleted = 0 " +
                    "ORDER BY id DESC";

    private static final String SQL_FIND_SOURCE_BY_ID =
            "SELECT id, name, source_type, source_url, enabled, created_at, updated_at " +
                    "FROM radar_source " +
                    "WHERE id = ? AND is_deleted = 0 " +
                    "LIMIT 1";

    private static final String SQL_UPDATE_SOURCE_ENABLED =
            "UPDATE radar_source SET enabled = ?, updated_at = ? " +
                    "WHERE id = ? AND is_deleted = 0";

    private static final String SQL_INSERT_TASK =
            "INSERT INTO radar_task " +
                    "(id, source_id, task_name, cron_expression, status, last_run_at, run_count, created_at, updated_at, is_deleted) " +
                    "VALUES (?, ?, ?, ?, ?, NULL, 0, ?, ?, 0)";

    private static final String SQL_LIST_TASK =
            "SELECT id, source_id, task_name, cron_expression, status, last_run_at, run_count, created_at, updated_at " +
                    "FROM radar_task " +
                    "WHERE is_deleted = 0 " +
                    "ORDER BY id DESC";

    private static final String SQL_FIND_TASK_BY_ID =
            "SELECT id, source_id, task_name, cron_expression, status, last_run_at, run_count, created_at, updated_at " +
                    "FROM radar_task " +
                    "WHERE id = ? AND is_deleted = 0 " +
                    "LIMIT 1";

    private static final String SQL_UPDATE_TASK_RUN =
            "UPDATE radar_task " +
                    "SET status = 'SUCCESS', last_run_at = ?, run_count = run_count + 1, updated_at = ? " +
                    "WHERE id = ? AND is_deleted = 0";

    private static final String SQL_NEXT_SOURCE_ID = "SELECT COALESCE(MAX(id), 1000) + 1 FROM radar_source";
    private static final String SQL_NEXT_TASK_ID = "SELECT COALESCE(MAX(id), 2000) + 1 FROM radar_task";

    private final JdbcTemplate jdbcTemplate;

    public MysqlRadarService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RadarSource createSource(String name, String sourceType, String sourceUrl, boolean enabled) {
        if (isBlank(name) || isBlank(sourceType) || isBlank(sourceUrl)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "name/sourceType/sourceUrl is required");
        }

        Long id = jdbcTemplate.queryForObject(SQL_NEXT_SOURCE_ID, Long.class);
        long now = System.currentTimeMillis();
        Timestamp nowTs = new Timestamp(now);

        jdbcTemplate.update(
                SQL_INSERT_SOURCE,
                id,
                name.trim(),
                sourceType.trim(),
                sourceUrl.trim(),
                enabled ? 1 : 0,
                nowTs,
                nowTs
        );

        return findSourceById(id);
    }

    @Override
    public List<RadarSource> listSources() {
        return jdbcTemplate.query(SQL_LIST_SOURCE, this::mapSource);
    }

    @Override
    public RadarSource setSourceEnabled(Long sourceId, boolean enabled) {
        if (sourceId == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "sourceId is required");
        }

        RadarSource source = findSourceById(sourceId);
        if (source == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "source not found: " + sourceId);
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.update(SQL_UPDATE_SOURCE_ENABLED, enabled ? 1 : 0, now, sourceId);
        return findSourceById(sourceId);
    }

    @Override
    public RadarTask createTask(Long sourceId, String taskName, String cronExpression) {
        if (sourceId == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "sourceId is required");
        }
        if (isBlank(taskName) || isBlank(cronExpression)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "taskName/cronExpression is required");
        }

        RadarSource source = findSourceById(sourceId);
        if (source == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "source not found: " + sourceId);
        }

        Long id = jdbcTemplate.queryForObject(SQL_NEXT_TASK_ID, Long.class);
        long now = System.currentTimeMillis();
        Timestamp nowTs = new Timestamp(now);

        jdbcTemplate.update(
                SQL_INSERT_TASK,
                id,
                sourceId,
                taskName.trim(),
                cronExpression.trim(),
                "CREATED",
                nowTs,
                nowTs
        );

        return findTaskById(id);
    }

    @Override
    public List<RadarTask> listTasks() {
        return jdbcTemplate.query(SQL_LIST_TASK, this::mapTask);
    }

    @Override
    public RadarTask runTask(Long taskId) {
        if (taskId == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "taskId is required");
        }

        RadarTask task = findTaskById(taskId);
        if (task == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "task not found: " + taskId);
        }

        RadarSource source = findSourceById(task.getSourceId());
        if (source == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "source not found: " + task.getSourceId());
        }
        if (!source.isEnabled()) {
            throw new BizException(ResultCode.BIZ_ERROR.getCode(), "source is disabled: " + source.getId());
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.update(SQL_UPDATE_TASK_RUN, now, now, taskId);
        return findTaskById(taskId);
    }

    private RadarSource findSourceById(Long sourceId) {
        try {
            return jdbcTemplate.queryForObject(SQL_FIND_SOURCE_BY_ID, new Object[]{sourceId}, this::mapSource);
        } catch (EmptyResultDataAccessException ignore) {
            return null;
        }
    }

    private RadarTask findTaskById(Long taskId) {
        try {
            return jdbcTemplate.queryForObject(SQL_FIND_TASK_BY_ID, new Object[]{taskId}, this::mapTask);
        } catch (EmptyResultDataAccessException ignore) {
            return null;
        }
    }

    private RadarSource mapSource(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new RadarSource(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("source_type"),
                rs.getString("source_url"),
                rs.getInt("enabled") == 1,
                createdAt == null ? 0L : createdAt.getTime(),
                updatedAt == null ? 0L : updatedAt.getTime()
        );
    }

    private RadarTask mapTask(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        Timestamp lastRunAt = rs.getTimestamp("last_run_at");

        RadarTask task = new RadarTask(
                rs.getLong("id"),
                rs.getLong("source_id"),
                rs.getString("task_name"),
                rs.getString("cron_expression"),
                rs.getString("status"),
                createdAt == null ? 0L : createdAt.getTime(),
                updatedAt == null ? 0L : updatedAt.getTime()
        );
        task.setLastRunAt(lastRunAt == null ? 0L : lastRunAt.getTime());
        task.setRunCount(rs.getLong("run_count"));
        return task;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
