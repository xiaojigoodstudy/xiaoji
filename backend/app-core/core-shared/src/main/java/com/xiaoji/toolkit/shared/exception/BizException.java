package com.xiaoji.toolkit.shared.exception;

import com.xiaoji.toolkit.shared.constants.ResultCode;

public class BizException extends RuntimeException {
    private final String code;

    public BizException(String message) {
        this(ResultCode.BIZ_ERROR.getCode(), message);
    }

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

