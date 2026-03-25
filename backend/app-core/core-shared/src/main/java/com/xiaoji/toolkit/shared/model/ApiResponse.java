package com.xiaoji.toolkit.shared.model;

import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.trace.TraceContext;

public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    private String traceId;
    private long timestamp;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<T>();
        response.code = ResultCode.SUCCESS.getCode();
        response.message = ResultCode.SUCCESS.getMessage();
        response.data = data;
        response.traceId = TraceContext.getTraceId();
        response.timestamp = System.currentTimeMillis();
        return response;
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        ApiResponse<T> response = new ApiResponse<T>();
        response.code = code;
        response.message = message;
        response.data = null;
        response.traceId = TraceContext.getTraceId();
        response.timestamp = System.currentTimeMillis();
        return response;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getTraceId() {
        return traceId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

