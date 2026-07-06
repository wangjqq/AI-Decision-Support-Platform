package com.aidsp.platform.company.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公司分析历史列表项（简版，用于历史列表渲染）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAnalysisHistoryItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String analysisId;
    private Long companyId;
    private long tookMs;
    private LocalDateTime createdAt;
    /** 5 维度标题拼接的简述："概览+主营+优势+风险+结论"。 */
    private String snippet;
}
