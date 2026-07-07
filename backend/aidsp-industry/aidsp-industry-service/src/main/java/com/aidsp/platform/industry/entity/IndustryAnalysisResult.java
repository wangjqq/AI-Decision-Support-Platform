package com.aidsp.platform.industry.entity;

import com.aidsp.platform.industry.api.IndustryAnalysisReferenceVO;
import com.aidsp.platform.industry.api.IndustryChainNodeVO;
import com.aidsp.platform.industry.api.IndustryDimensionVO;
import com.aidsp.platform.industry.api.IndustryAnalysisResultVO;
import com.aidsp.platform.industry.api.IndustryLeadingCompanyVO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 行业分析结果（领域对象，与 VO 同构）。
 */
@Data
public class IndustryAnalysisResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String analysisId;
    private Long industryId;
    private String industryName;
    private long tookMs;
    private LocalDateTime createdAt;

    private IndustryDimensionVO overview;
    private IndustryDimensionVO marketSize;
    private IndustryDimensionVO chain;
    private IndustryDimensionVO leading;
    private IndustryDimensionVO trends;
    private IndustryDimensionVO risks;

    private List<IndustryChainNodeVO> chainNodes;
    private List<IndustryLeadingCompanyVO> leadingCompanies;

    private List<IndustryAnalysisReferenceVO> references;
}
