package com.xiaoji.toolkit.habit.service;

import com.xiaoji.toolkit.habit.model.HabitCheckinRecord;
import com.xiaoji.toolkit.habit.model.HabitItem;
import com.xiaoji.toolkit.habit.model.HabitStats;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Profile("mysql")
public class MysqlHabitService implements HabitService {

    private static final String REDIS_KEY_TODAY_PREFIX = "xiaoji:habit:checkin:";

    private static final String SQL_INSERT_ITEM =
            "INSERT INTO habit_item (id, name, enabled, created_at, updated_at, is_deleted) " +
                    "VALUES (?, ?, ?, ?, ?, 0)";

    private static final String SQL_LIST_ITEMS =
            "SELECT id, name, enabled, created_at, updated_at " +
                    "FROM habit_item " +
                    "WHERE is_deleted = 0 " +
                    "ORDER BY id DESC";

    private static final String SQL_FIND_ITEM_BY_ID =
            "SELECT id, name, enabled, created_at, updated_at " +
                    "FROM habit_item " +
                    "WHERE id = ? AND is_deleted = 0 " +
                    "LIMIT 1";

    private static final String SQL_INSERT_CHECKIN =
            "INSERT INTO habit_checkin_record (id, habit_item_id, user_id, checkin_date, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_LIST_CHECKINS =
            "SELECT id, habit_item_id, user_id, checkin_date, created_at " +
                    "FROM habit_checkin_record " +
                    "ORDER BY id DESC";

    private static final String SQL_COUNT_ITEMS = "SELECT COUNT(1) FROM habit_item WHERE is_deleted = 0";
    private static final String SQL_COUNT_TOTAL_CHECKIN = "SELECT COUNT(1) FROM habit_checkin_record";
    private static final String SQL_COUNT_TODAY_CHECKIN = "SELECT COUNT(1) FROM habit_checkin_record WHERE checkin_date = ?";

    private static final String SQL_NEXT_ITEM_ID = "SELECT COALESCE(MAX(id), 8000) + 1 FROM habit_item";
    private static final String SQL_NEXT_CHECKIN_ID = "SELECT COALESCE(MAX(id), 9000) + 1 FROM habit_checkin_record";

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    public MysqlHabitService(JdbcTemplate jdbcTemplate, ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    @Override
    public HabitItem createHabitItem(String name, boolean enabled) {
        if (isBlank(name)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "habit name is required");
        }

        Long id = jdbcTemplate.queryForObject(SQL_NEXT_ITEM_ID, Long.class);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        jdbcTemplate.update(SQL_INSERT_ITEM, id, name.trim(), enabled ? 1 : 0, now, now);

        HabitItem item = findHabitItemById(id);
        if (item == null) {
            throw new BizException(ResultCode.INTERNAL_ERROR.getCode(), "failed to create habit item");
        }
        return item;
    }

    @Override
    public List<HabitItem> listHabitItems() {
        return jdbcTemplate.query(SQL_LIST_ITEMS, this::mapHabitItem);
    }

    @Override
    public HabitCheckinRecord checkin(Long habitItemId, Long userId, String checkinDate) {
        if (habitItemId == null || userId == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "habitItemId and userId are required");
        }

        HabitItem item = findHabitItemById(habitItemId);
        if (item == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "habit item not found: " + habitItemId);
        }
        if (!item.isEnabled()) {
            throw new BizException(ResultCode.BIZ_ERROR.getCode(), "habit item is disabled: " + habitItemId);
        }

        LocalDate normalizedDate = parseOrNow(checkinDate);
        String dateString = normalizedDate.toString();
        Long checkinId = jdbcTemplate.queryForObject(SQL_NEXT_CHECKIN_ID, Long.class);
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try {
            jdbcTemplate.update(SQL_INSERT_CHECKIN, checkinId, habitItemId, userId, dateString, now);
        } catch (DuplicateKeyException ex) {
            throw new BizException(ResultCode.BIZ_ERROR.getCode(), "already checked in for this date");
        }

        if (redisTemplate != null) {
            try {
                String key = REDIS_KEY_TODAY_PREFIX + dateString;
                redisTemplate.opsForValue().increment(key);
                redisTemplate.expire(key, 2, TimeUnit.DAYS);
            } catch (Exception ignore) {
                // Redis stats cache is optional.
            }
        }

        return new HabitCheckinRecord(checkinId, habitItemId, userId, dateString, now.getTime());
    }

    @Override
    public List<HabitCheckinRecord> listCheckinRecords() {
        return jdbcTemplate.query(SQL_LIST_CHECKINS, this::mapCheckin);
    }

    @Override
    public HabitStats stats() {
        Integer itemCount = jdbcTemplate.queryForObject(SQL_COUNT_ITEMS, Integer.class);
        Integer totalCheckinCount = jdbcTemplate.queryForObject(SQL_COUNT_TOTAL_CHECKIN, Integer.class);

        LocalDate today = LocalDate.now();
        String todayKey = REDIS_KEY_TODAY_PREFIX + today.toString();
        Integer todayCheckinCount = null;

        if (redisTemplate != null) {
            try {
                String cached = redisTemplate.opsForValue().get(todayKey);
                if (cached != null && !cached.trim().isEmpty()) {
                    todayCheckinCount = Integer.valueOf(cached);
                }
            } catch (Exception ignore) {
                // ignore redis read failures and fallback to DB.
            }
        }

        if (todayCheckinCount == null) {
            todayCheckinCount = jdbcTemplate.queryForObject(SQL_COUNT_TODAY_CHECKIN, new Object[]{today.toString()}, Integer.class);
            if (redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set(todayKey, String.valueOf(todayCheckinCount), 2, TimeUnit.DAYS);
                } catch (Exception ignore) {
                    // ignore redis write failures.
                }
            }
        }

        return new HabitStats(
                itemCount == null ? 0 : itemCount,
                totalCheckinCount == null ? 0 : totalCheckinCount,
                todayCheckinCount == null ? 0 : todayCheckinCount
        );
    }

    private HabitItem findHabitItemById(Long habitItemId) {
        List<HabitItem> items = jdbcTemplate.query(SQL_FIND_ITEM_BY_ID, new Object[]{habitItemId}, this::mapHabitItem);
        return items.isEmpty() ? null : items.get(0);
    }

    private HabitItem mapHabitItem(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new HabitItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getInt("enabled") == 1,
                createdAt == null ? 0L : createdAt.getTime(),
                updatedAt == null ? 0L : updatedAt.getTime()
        );
    }

    private HabitCheckinRecord mapCheckin(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new HabitCheckinRecord(
                rs.getLong("id"),
                rs.getLong("habit_item_id"),
                rs.getLong("user_id"),
                rs.getString("checkin_date"),
                createdAt == null ? 0L : createdAt.getTime()
        );
    }

    private LocalDate parseOrNow(String checkinDate) {
        if (isBlank(checkinDate)) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(checkinDate.trim());
        } catch (DateTimeParseException ex) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "invalid checkinDate format, expected yyyy-MM-dd");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
