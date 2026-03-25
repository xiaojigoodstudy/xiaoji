package com.xiaoji.toolkit.bootstrap.api;

import com.xiaoji.toolkit.shared.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("status", "UP");
        payload.put("service", "daily-toolkit-backend");
        payload.put("timestamp", System.currentTimeMillis());
        return ApiResponse.success(payload);
    }
}

