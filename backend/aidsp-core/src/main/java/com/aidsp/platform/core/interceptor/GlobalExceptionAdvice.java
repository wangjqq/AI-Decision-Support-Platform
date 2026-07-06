package com.aidsp.platform.core.interceptor;

import com.aidsp.platform.core.dto.RestResponse;
import com.aidsp.platform.core.exception.BusinessException;
import com.aidsp.platform.core.exception.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理 Advice。
 *
 * <p>异常 → 业务码映射（参考 docs/api-spec.md §3.2）。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(BusinessException.class)
    public RestResponse<Void> handleBusiness(BusinessException e) {
        log.warn("[BusinessException] code={}, msg={}", e.getCode(), e.getMessage());
        return RestResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : ErrorCode.PARAM_INVALID.getMsg();
        log.warn("[Validation] {}", msg);
        return RestResponse.fail(ErrorCode.PARAM_INVALID.getCode(), msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public RestResponse<Void> handleConstraint(ConstraintViolationException e) {
        log.warn("[ConstraintViolation] {}", e.getMessage());
        return RestResponse.fail(ErrorCode.PARAM_INVALID);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    public RestResponse<Void> handleBadRequest(Exception e) {
        log.warn("[BadRequest] {}", e.getMessage());
        return RestResponse.fail(ErrorCode.PARAM_INVALID.getCode(), ErrorCode.PARAM_INVALID.getMsg());
    }

    @ExceptionHandler(Throwable.class)
    public RestResponse<Void> handleUnknown(Throwable e) {
        log.error("[SystemError]", e);
        return RestResponse.fail(ErrorCode.SYSTEM_ERROR);
    }
}
