package com.xiaoji.toolkit.notification.service;

import com.xiaoji.toolkit.notification.model.NotificationLog;
import com.xiaoji.toolkit.notification.model.SendNotificationCommand;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("mysql")
public class MysqlNotificationService implements NotificationService {

    private static final String REDIS_KEY_RECENT_LOGS = "xiaoji:notification:recent";

    private static final String SQL_INSERT_TASK =
            "INSERT INTO notification_task " +
                    "(id, title, content, target, channels_json, status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_LOG =
            "INSERT INTO notification_log " +
                    "(id, task_id, channel, target, title, content, status, detail, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_LIST_LOGS =
            "SELECT id, channel, target, title, content, status, detail, created_at " +
                    "FROM notification_log " +
                    "ORDER BY id DESC";

    private static final String SQL_NEXT_TASK_ID = "SELECT COALESCE(MAX(id), 4000) + 1 FROM notification_task";
    private static final String SQL_NEXT_LOG_ID = "SELECT COALESCE(MAX(id), 5000) + 1 FROM notification_log";

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final Map<String, NotificationChannelSender> senderByCode = new ConcurrentHashMap<String, NotificationChannelSender>();

    public MysqlNotificationService(JdbcTemplate jdbcTemplate,
                                    ObjectProvider<StringRedisTemplate> redisTemplateProvider,
                                    List<NotificationChannelSender> senders) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        if (senders != null) {
            for (NotificationChannelSender sender : senders) {
                senderByCode.put(sender.channelCode().toUpperCase(Locale.ROOT), sender);
            }
        }
    }

    @Override
    public List<NotificationLog> send(SendNotificationCommand command) {
        validate(command);

        List<String> channels = normalizeChannels(command.getChannels());
        Long taskId = jdbcTemplate.queryForObject(SQL_NEXT_TASK_ID, Long.class);
        Timestamp now = new Timestamp(System.currentTimeMillis());

        jdbcTemplate.update(
                SQL_INSERT_TASK,
                taskId,
                command.getTitle().trim(),
                command.getContent().trim(),
                command.getTarget().trim(),
                toJsonArrayOfStrings(channels),
                "SUCCESS",
                now,
                now
        );

        List<NotificationLog> result = new ArrayList<NotificationLog>();
        for (String channel : channels) {
            NotificationChannelSender sender = senderByCode.get(channel);
            if (sender == null) {
                throw new BizException(ResultCode.BAD_REQUEST.getCode(), "unsupported channel: " + channel);
            }

            String detail = sender.send(command.getTitle(), command.getContent(), command.getTarget());
            Long logId = jdbcTemplate.queryForObject(SQL_NEXT_LOG_ID, Long.class);
            Timestamp logTime = new Timestamp(System.currentTimeMillis());

            jdbcTemplate.update(
                    SQL_INSERT_LOG,
                    logId,
                    taskId,
                    channel,
                    command.getTarget().trim(),
                    command.getTitle().trim(),
                    command.getContent().trim(),
                    "SUCCESS",
                    detail,
                    logTime
            );

            if (redisTemplate != null) {
                try {
                    redisTemplate.opsForList().leftPush(REDIS_KEY_RECENT_LOGS, String.valueOf(logId));
                    redisTemplate.opsForList().trim(REDIS_KEY_RECENT_LOGS, 0, 199);
                } catch (Exception ignore) {
                    // Redis is auxiliary for recent logs and should not affect main write path.
                }
            }

            result.add(new NotificationLog(
                    logId,
                    channel,
                    command.getTarget().trim(),
                    command.getTitle().trim(),
                    command.getContent().trim(),
                    "SUCCESS",
                    detail,
                    logTime.getTime()
            ));
        }

        return result;
    }

    @Override
    public List<NotificationLog> listLogs() {
        return jdbcTemplate.query(SQL_LIST_LOGS, this::mapLog);
    }

    private NotificationLog mapLog(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new NotificationLog(
                rs.getLong("id"),
                rs.getString("channel"),
                rs.getString("target"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("status"),
                rs.getString("detail"),
                createdAt == null ? 0L : createdAt.getTime()
        );
    }

    private List<String> normalizeChannels(List<String> channels) {
        List<String> result = new ArrayList<String>();
        for (String channel : channels) {
            if (channel == null) {
                continue;
            }
            String normalized = channel.trim().toUpperCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        if (result.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "channels is required");
        }
        return result;
    }

    private String toJsonArrayOfStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append("\"").append(values.get(i)).append("\"");
        }
        builder.append("]");
        return builder.toString();
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
