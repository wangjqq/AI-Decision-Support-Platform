package com.aidsp.platform.company.entity;

import com.aidsp.platform.company.api.CompanyAnalysisReferenceVO;
import com.aidsp.platform.company.api.CompanyAnalysisResultVO;
import com.aidsp.platform.company.api.CompanyDimensionVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公司分析结果（领域对象，与 VO 同构）。
 */
@Data
public class CompanyAnalysisResult implements Serializable {

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
