package com.xiaoji.toolkit.ruleengine.service;

import com.xiaoji.toolkit.ruleengine.model.RuleDefinition;
import com.xiaoji.toolkit.ruleengine.model.RuleEvaluateResult;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Profile("mysql")
public class MysqlRuleEngineService implements RuleEngineService {

    private static final String REDIS_KEY_EVALUATE_COUNT = "xiaoji:rule:evaluate:count";
    private static final String REDIS_KEY_EVALUATE_LAST_TS = "xiaoji:rule:evaluate:last_ts";

    private static final String SQL_INSERT_RULE =
            "INSERT INTO rule_definition " +
                    "(id, rule_name, keyword, source_type, notify_channel, enabled, created_at, updated_at, is_deleted) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";

    private static final String SQL_LIST_RULE =
            "SELECT id, rule_name, keyword, source_type, notify_channel, enabled, created_at, updated_at " +
                    "FROM rule_definition " +
                    "WHERE is_deleted = 0 " +
                    "ORDER BY id DESC";

    private static final String SQL_LIST_ENABLED_RULE =
            "SELECT id, rule_name, keyword, source_type, notify_channel, enabled, created_at, updated_at " +
                    "FROM rule_definition " +
                    "WHERE is_deleted = 0 AND enabled = 1";

    private static final String SQL_FIND_RULE_BY_ID =
            "SELECT id, rule_name, keyword, source_type, notify_channel, enabled, created_at, updated_at " +
                    "FROM rule_definition " +
                    "WHERE id = ? AND is_deleted = 0 " +
                    "LIMIT 1";

    private static final String SQL_UPDATE_RULE_ENABLED =
            "UPDATE rule_definition SET enabled = ?, updated_at = ? " +
                    "WHERE id = ? AND is_deleted = 0";

    private static final String SQL_INSERT_EVALUATE_LOG =
            "INSERT INTO rule_evaluate_log (id, content, source_type, matched_rule_ids_json, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_NEXT_RULE_ID = "SELECT COALESCE(MAX(id), 3000) + 1 FROM rule_definition";
    private static final String SQL_NEXT_EVALUATE_LOG_ID = "SELECT COALESCE(MAX(id), 6000) + 1 FROM rule_evaluate_log";

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    public MysqlRuleEngineService(JdbcTemplate jdbcTemplate, ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    @Override
    public RuleDefinition createRule(String ruleName,
                                     String keyword,
                                     String sourceType,
                                     String notifyChannel,
                                     boolean enabled) {
        if (isBlank(ruleName) || isBlank(keyword) || isBlank(notifyChannel)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "ruleName/keyword/notifyChannel is required");
        }

        Long id = jdbcTemplate.queryForObject(SQL_NEXT_RULE_ID, Long.class);
        Timestamp now = new Timestamp(System.currentTimeMillis());

        jdbcTemplate.update(
                SQL_INSERT_RULE,
                id,
                ruleName.trim(),
                keyword.trim(),
                trimNullable(sourceType),
                notifyChannel.trim(),
                enabled ? 1 : 0,
                now,
                now
        );

        return findRuleById(id);
    }

    @Override
    public List<RuleDefinition> listRules() {
        return jdbcTemplate.query(SQL_LIST_RULE, this::mapRule);
    }

    @Override
    public RuleDefinition setRuleEnabled(Long ruleId, boolean enabled) {
        if (ruleId == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "ruleId is required");
        }

        RuleDefinition existing = findRuleById(ruleId);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "rule not found: " + ruleId);
        }

        jdbcTemplate.update(SQL_UPDATE_RULE_ENABLED, enabled ? 1 : 0, new Timestamp(System.currentTimeMillis()), ruleId);
        return findRuleById(ruleId);
    }

    @Override
    public RuleEvaluateResult evaluate(String content, String sourceType) {
        if (isBlank(content)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "content is required");
        }

        String lowerContent = content.toLowerCase(Locale.ROOT);
        String normalizedSourceType = trimNullable(sourceType);
        List<RuleDefinition> enabledRules = jdbcTemplate.query(SQL_LIST_ENABLED_RULE, this::mapRule);

        List<RuleDefinition> matched = new ArrayList<RuleDefinition>();
        for (RuleDefinition rule : enabledRules) {
            if (!lowerContent.contains(rule.getKeyword().toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (!isBlank(rule.getSourceType()) && !rule.getSourceType().equalsIgnoreCase(normalizedSourceType)) {
                continue;
            }
            matched.add(rule);
        }

        Long logId = jdbcTemplate.queryForObject(SQL_NEXT_EVALUATE_LOG_ID, Long.class);
        jdbcTemplate.update(
                SQL_INSERT_EVALUATE_LOG,
                logId,
                content,
                normalizedSourceType,
                toJsonArrayOfIds(matched),
                new Timestamp(System.currentTimeMillis())
        );

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().increment(REDIS_KEY_EVALUATE_COUNT);
                redisTemplate.opsForValue().set(REDIS_KEY_EVALUATE_LAST_TS, String.valueOf(System.currentTimeMillis()));
            } catch (Exception ignore) {
                // Redis is an auxiliary metric path; do not break main flow.
            }
        }

        return new RuleEvaluateResult(content, normalizedSourceType, matched);
    }

    private RuleDefinition findRuleById(Long ruleId) {
        try {
            return jdbcTemplate.queryForObject(SQL_FIND_RULE_BY_ID, new Object[]{ruleId}, this::mapRule);
        } catch (EmptyResultDataAccessException ignore) {
            return null;
        }
    }

    private RuleDefinition mapRule(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new RuleDefinition(
                rs.getLong("id"),
                rs.getString("rule_name"),
                rs.getString("keyword"),
                rs.getString("source_type"),
                rs.getString("notify_channel"),
                rs.getInt("enabled") == 1,
                createdAt == null ? 0L : createdAt.getTime(),
                updatedAt == null ? 0L : updatedAt.getTime()
        );
    }

    private String toJsonArrayOfIds(List<RuleDefinition> rules) {
        if (rules == null || rules.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < rules.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(rules.get(i).getId());
        }
        builder.append("]");
        return builder.toString();
    }

    private static String trimNullable(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
