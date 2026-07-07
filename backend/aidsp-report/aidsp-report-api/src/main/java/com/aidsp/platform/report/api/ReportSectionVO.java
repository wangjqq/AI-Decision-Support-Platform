package com.aidsp.platform.report.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报告中的单一章节（行业分析 / 公司分析 / 风险分析 / 最终结论）。
 * <p>每个章节对应 Markdown 文档中一个 H2 段落。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSectionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 章节 KEY：industry / company / risk / conclusion。 */
    private String key;

    /** 章节标题（中文，用于目录与正文）。 */
    private String title;

    /** 锚点 ID（前端路由跳转用）。 */
    private String anchor;

    /** 章节 Markdown 原文（含标题、列表、表格等）。 */
    private String markdown;
}
