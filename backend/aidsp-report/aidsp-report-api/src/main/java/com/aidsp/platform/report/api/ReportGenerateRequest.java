package com.aidsp.platform.report.api;

import com.aidsp.platform.company.api.CompanyAnalysisResultVO;
import com.aidsp.platform.industry.api.IndustryAnalysisResultVO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 触发报告生成的请求体。
 * <p>前端从「公司分析」或「行业分析」页发起，传入对应分析结果，
 * <br>后端交给 ReportGenerationAgent 生成 Markdown 报告。
 * <p>采用「嵌入式数据」方案：前端已经持有分析结果，直接传过来，
 * <br>避免在 report 模块强依赖 company / industry 模块的具体服务。
 */
@Data
public class ReportGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 公司 ID。 */
    @NotNull
    private Long companyId;

    /** 公司名（冗余，便于报告标题生成与展示）。 */
    private String companyName;

    /** 公司分析 ID。 */
    @NotNull
    private String companyAnalysisId;

    /** 公司分析结果（必填）。 */
    @NotNull
    private CompanyAnalysisResultVO companyAnalysis;

    /** 行业 ID（可选）。 */
    private Long industryId;

    /** 行业名（冗余）。 */
    private String industryName;

    /** 行业分析 ID（可选）。 */
    private String industryAnalysisId;

    /** 行业分析结果（可选）。 */
    private IndustryAnalysisResultVO industryAnalysis;

    /** 报告标题（可选，留空则由 Agent 自动生成）。 */
    private String title;

    /** 附加 query（可选，透传给 Agent）。 */
    private String query;
}
