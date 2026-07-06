package com.aidsp.platform.core.exception;

import lombok.Getter;

/**
 * 业务异常。code 字段为业务状态码（非 HTTP 状态码）。
 *
 * <p>使用：
 * <pre>{@code
 * throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
 * throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND, "公司不存在: " + id);
 * }</pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
