package com.aidsp.platform.industry.api;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 行业分析结果（6 维度 + 产业链 + 龙头企业 + 元数据）。
 */
@Data
public class IndustryAnalysisResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String analysisId;
    private Long industryId;
    private String industryName;
    private long tookMs;
    private LocalDateTime createdAt;

    /** 行业概况。 */
    private IndustryDimensionVO overview;
    /** 市场空间。 */
    private IndustryDimensionVO marketSize;
    /** 产业链结构（摘要 + 节点列表）。 */
    private IndustryDimensionVO chain;
    /** 龙头企业（摘要 + 列表）。 */
    private IndustryDimensionVO leading;
    /** 未来趋势。 */
    private IndustryDimensionVO trends;
    /** 风险分析。 */
    private IndustryDimensionVO risks;

    /** 产业链节点明细。 */
    private List<IndustryChainNodeVO> chainNodes;
    /** 龙头企业明细。 */
    private List<IndustryLeadingCompanyVO> leadingCompanies;

    private List<IndustryAnalysisReferenceVO> references;
}
