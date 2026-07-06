package com.aidsp.platform.core.dto;

import com.aidsp.platform.core.exception.ErrorCode;
import lombok.Data;

/**
 * 统一返回结构。
 *
 * <p>Controller 直返业务对象，由 {@code GlobalRestResponseAdvice} 自动包装。
 * 字段定义见 docs/api-spec.md §2.1。
 *
 * @param <T> 业务数据类型
 */
@Data
public class RestResponse<T> {

    /** 业务状态码：0 表示成功，非 0 表示业务错误。 */
    private int code;

    /** 提示信息。 */
    private String msg;

    /** 业务数据。 */
    private T data;

    /** 链路追踪 ID。 */
    private String traceId;

    /** 服务器时间戳（毫秒）。 */
    private long timestamp;

    public RestResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public RestResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /** 成功响应（业务对象）。 */
    public static <T> RestResponse<T> success(T data) {
        return new RestResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg(), data);
    }

    /** 失败响应（自定义 code + msg）。 */
    public static <T> RestResponse<T> fail(int code, String msg) {
        return new RestResponse<>(code, msg, null);
    }

    /** 失败响应（枚举 ErrorCode）。 */
    public static <T> RestResponse<T> fail(ErrorCode errorCode) {
        return new RestResponse<>(errorCode.getCode(), errorCode.getMsg(), null);
    }
}
