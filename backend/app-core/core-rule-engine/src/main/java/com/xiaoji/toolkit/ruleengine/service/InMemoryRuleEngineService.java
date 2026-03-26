package com.xiaoji.toolkit.ruleengine.service;

import com.xiaoji.toolkit.ruleengine.model.RuleDefinition;
import com.xiaoji.toolkit.ruleengine.model.RuleEvaluateResult;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("!mysql")
public class InMemoryRuleEngineService implements RuleEngineService {

    private final AtomicLong ruleIdGen = new AtomicLong(3000L);
    private final ConcurrentHashMap<Long, RuleDefinition> ruleStore = new ConcurrentHashMap<Long, RuleDefinition>();

    @Override
    public RuleDefinition createRule(String ruleName,
                                     String keyword,
                                     String sourceType,
                                     String notifyChannel,
                                     boolean enabled) {
        if (isBlank(ruleName) || isBlank(keyword) || isBlank(notifyChannel)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "ruleName/keyword/notifyChannel is required");
        }

        long now = System.currentTimeMillis();
        Long id = ruleIdGen.incrementAndGet();
        RuleDefinition rule = new RuleDefinition(
                id,
                ruleName.trim(),
                keyword.trim(),
                trimNullable(sourceType),
                notifyChannel.trim(),
                enabled,
                now,
                now
        );
        ruleStore.put(id, rule);
        return rule;
    }

    @Override
    public List<RuleDefinition> listRules() {
        return new ArrayList<RuleDefinition>(ruleStore.values());
    }

    @Override
    public RuleDefinition setRuleEnabled(Long ruleId, boolean enabled) {
        RuleDefinition rule = ruleStore.get(ruleId);
        if (rule == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "rule not found: " + ruleId);
        }
        rule.setEnabled(enabled);
        rule.setUpdatedAt(System.currentTimeMillis());
        return rule;
    }

    @Override
    public RuleEvaluateResult evaluate(String content, String sourceType) {
        if (isBlank(content)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "content is required");
        }

        String lowerContent = content.toLowerCase(Locale.ROOT);
        String normalizedSourceType = trimNullable(sourceType);

        List<RuleDefinition> matched = new ArrayList<RuleDefinition>();
        for (RuleDefinition rule : ruleStore.values()) {
            if (!rule.isEnabled()) {
                continue;
            }

            if (!lowerContent.contains(rule.getKeyword().toLowerCase(Locale.ROOT))) {
                continue;
            }

            if (!isBlank(rule.getSourceType()) && !rule.getSourceType().equalsIgnoreCase(normalizedSourceType)) {
                continue;
            }

            matched.add(rule);
        }

        return new RuleEvaluateResult(content, normalizedSourceType, matched);
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
