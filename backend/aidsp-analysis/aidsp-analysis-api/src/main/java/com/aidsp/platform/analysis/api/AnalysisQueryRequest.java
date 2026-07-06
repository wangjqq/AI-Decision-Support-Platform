package com.aidsp.platform.analysis.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 智能分析请求 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户原始问题。 */
    @NotBlank(message = "query 不能为空")
    @Size(max = 500, message = "query 长度不能超过 500")
    private String query;

    /** 返回条数，1~20，默认 5。 */
    @Builder.Default
    @Min(value = 1, message = "topK 最小为 1")
    @Max(value = 20, message = "topK 最大为 20")
    private Integer topK = 5;

    /** 上下文信息（透传至 Agent）。 */
    private Map<String, Object> context;
}
