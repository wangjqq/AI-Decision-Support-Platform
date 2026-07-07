package com.aidsp.platform.report.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报告中的单一章节（行业分析 / 公司分析 / 风险分析 / 最终结论）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSection implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 章节 KEY。 */
    private String key;

    /** 章节中文标题。 */
    private String title;

    /** 锚点 ID。 */
    private String anchor;

    /** 章节 Markdown 原文。 */
    private String markdown;
}
