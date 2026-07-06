package com.aidsp.platform.company.api;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公司分析结果（5 维度 + 元数据）。
 */
@Data
public class CompanyAnalysisResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String analysisId;
    private Long companyId;
    private String companyName;
    private long tookMs;
    private LocalDateTime createdAt;

    private CompanyDimensionVO overview;
    private CompanyDimensionVO mainBusiness;
    private CompanyDimensionVO advantages;
    private CompanyDimensionVO risks;
    private CompanyDimensionVO aiConclusion;

    private List<CompanyAnalysisReferenceVO> references;
}
