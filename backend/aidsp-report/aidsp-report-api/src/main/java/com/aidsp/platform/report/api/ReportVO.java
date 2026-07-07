package com.aidsp.platform.report.api;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报告详情 VO。
 * <p>对应 {@code /api/v1/reports/{id}} 响应体，包含 5 大区块的 Markdown 文本、目录、参考引用。
 */
@Data
public class ReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 报告 ID。 */
    private String reportId;

    /** 报告标题。 */
    private String title;

    /** 报告类型：COMPANY（公司画像）/ INDUSTRY（行业研究）/ COMPREHENSIVE（综合研报）。 */
    private String type;

    /** 报告状态。 */
    private String status;

    /** 公司 ID。 */
    private Long companyId;

    /** 公司名。 */
    private String companyName;

    /** 行业 ID（可选）。 */
    private Long industryId;

    /** 行业名（可选）。 */
    private String industryName;

    /** 公司分析 ID。 */
    private String companyAnalysisId;

    /** 行业分析 ID（可选）。 */
    private String industryAnalysisId;

    /** 摘要（1-2 段）。 */
    private String summary;

    /** 摘要 Markdown 文本（含小标题与要点）。 */
    private String summaryMarkdown;

    /** 目录。 */
    private List<ReportTocItemVO> toc;

    /** 章节列表（行业分析 / 公司分析 / 风险分析 / 最终结论）。 */
    private List<ReportSectionVO> sections;

    /** 完整 Markdown（用于导出/全屏阅读）。 */
    private String markdown;

    /** 参考引用。 */
    private List<ReportReferenceVO> references;

    /** 生成耗时（毫秒）。 */
    private long tookMs;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
