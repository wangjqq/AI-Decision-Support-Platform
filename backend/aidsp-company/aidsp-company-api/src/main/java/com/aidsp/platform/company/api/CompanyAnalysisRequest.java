package com.aidsp.platform.company.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 触发公司分析的请求。
 */
@Data
public class CompanyAnalysisRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户附加问题（可选，留空则由 Agent 自主分析）。 */
    @Size(max = 500)
    private String query;

    /** TopK，控制摘要/要点条数。默认 5。 */
    @Min(1)
    @Max(20)
    private Integer topK = 5;

    /** 透传至 Agent 的上下文。 */
    private Map<String, Object> context;
}
