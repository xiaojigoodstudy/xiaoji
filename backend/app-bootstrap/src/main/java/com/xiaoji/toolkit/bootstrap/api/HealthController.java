package com.xiaoji.toolkit.bootstrap.api;

import com.xiaoji.toolkit.shared.model.ApiResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    public HealthController(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.redisTemplateProvider = redisTemplateProvider;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("status", "UP");
        payload.put("service", "daily-toolkit-backend");
        payload.put("redis", detectRedisStatus());
        payload.put("timestamp", System.currentTimeMillis());
        return ApiResponse.success(payload);
    }

    private String detectRedisStatus() {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return "DISABLED";
        }

        try {
            String pong = redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
            return "PONG".equalsIgnoreCase(pong) ? "UP" : "DOWN";
        } catch (Exception ignore) {
            return "DOWN";
        }
    }
}

