package com.aidsp.platform.company.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 公司分析结果中的单一维度（公司概览 / 主营业务 / 核心优势 / 潜在风险 / AI 结论）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDimensionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 维度标题。 */
    private String title;

    /** 图标名（前端用 antd icon 名映射）。 */
    private String icon;

    /** 主题色（blue/green/orange/red/purple）。 */
    private String color;

    /** 1~2 段总结。 */
    private String summary;

    /** 关键要点（3~5 条）。 */
    private List<String> keyPoints;

    /** 可选指标（如毛利率 / PE 等）。 */
    private Map<String, String> metrics;
}
