package com.xiaoji.toolkit.ruleengine.model;

import java.util.ArrayList;
import java.util.List;

public class RuleEvaluateResult {
    private final String content;
    private final String sourceType;
    private final List<RuleDefinition> matchedRules;

    public RuleEvaluateResult(String content, String sourceType, List<RuleDefinition> matchedRules) {
        this.content = content;
        this.sourceType = sourceType;
        this.matchedRules = matchedRules == null ? new ArrayList<RuleDefinition>() : matchedRules;
    }

    public String getContent() {
        return content;
    }

    public String getSourceType() {
        return sourceType;
    }

    public List<RuleDefinition> getMatchedRules() {
        return matchedRules;
    }
}
