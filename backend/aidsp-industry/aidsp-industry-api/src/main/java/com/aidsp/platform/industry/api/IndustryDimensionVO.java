package com.aidsp.platform.industry.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 行业分析结果中的单一维度（行业概况 / 市场空间 / 产业链结构 / 龙头企业 / 未来趋势 / 风险分析）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryDimensionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 维度标题。 */
    private String title;

    /** 图标名（前端用 antd icon 名映射）。 */
    private String icon;

    /** 主题色（blue/green/orange/red/purple/gold/cyan）。 */
    private String color;

    /** 1~2 段总结。 */
    private String summary;

    /** 关键要点（3~5 条）。 */
    private List<String> keyPoints;

    /** 可选指标（如市场规模 / CAGR / 集中度 等）。 */
    private Map<String, String> metrics;
}
