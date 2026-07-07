package com.aidsp.platform.report.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报告实体（内存版，MVP 阶段不接 DB）。
 * <p>保存 ReportGenerationAgent 生成的完整 Markdown 报告，含 5 大章节、目录、参考引用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 报告 ID（格式：rp-yyyyMMdd-xxx）。 */
    private String reportId;

    /** 报告标题。 */
    private String title;

    /** 报告类型：COMPANY / INDUSTRY / COMPREHENSIVE。 */
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

    /** 关联的公司分析 ID。 */
    private String companyAnalysisId;

    /** 关联的行业分析 ID（可选）。 */
    private String industryAnalysisId;

    /** 摘要纯文本（120 字以内）。 */
    private String summary;

    /** 摘要 Markdown 文本。 */
    private String summaryMarkdown;

    /** 目录条目列表。 */
    private List<ReportTocItem> toc;

    /** 章节列表。 */
    private List<ReportSection> sections;

    /** 完整 Markdown 文本。 */
    private String markdown;

    /** 参考引用。 */
    private List<ReportReference> references;

    /** 生成耗时（毫秒）。 */
    private long tookMs;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
