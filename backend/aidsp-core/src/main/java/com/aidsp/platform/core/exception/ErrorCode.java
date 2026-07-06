package com.aidsp.platform.core.exception;

import lombok.Getter;

/**
 * 业务状态码枚举。
 *
 * <p>码段（参考 docs/api-spec.md §2.5）：
 * <ul>
 *   <li>0    成功</li>
 *   <li>1xxx 通用错误</li>
 *   <li>2xxx 公司模块</li>
 *   <li>3xxx 行业模块</li>
 *   <li>4xxx 报告模块</li>
 *   <li>5xxx Agent / AI 错误</li>
 *   <li>6xxx 知识库 / RAG 错误</li>
 *   <li>9xxx 第三方服务错误</li>
 * </ul>
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "success"),

    // 1xxx 通用错误
    SYSTEM_ERROR(1000, "系统繁忙"),
    PARAM_INVALID(1001, "参数校验失败"),
    UNAUTHORIZED(1002, "未授权"),
    FORBIDDEN(1003, "无权限"),
    NOT_FOUND(1004, "资源不存在"),
    CONFLICT(1005, "资源冲突"),

    // 2xxx 公司模块
    COMPANY_NOT_FOUND(2001, "公司不存在"),

    // 3xxx 行业模块
    INDUSTRY_NOT_FOUND(3001, "行业不存在"),

    // 4xxx 报告模块
    REPORT_NOT_FOUND(4001, "报告不存在"),

    // 5xxx Agent / AI 错误
    AGENT_RUN_FAILED(5000, "Agent 执行失败"),

    // 6xxx 知识库 / RAG 错误
    KNOWLEDGE_NOT_FOUND(6001, "知识不存在");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
