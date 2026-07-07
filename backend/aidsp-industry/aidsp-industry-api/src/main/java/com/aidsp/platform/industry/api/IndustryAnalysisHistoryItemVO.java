package com.aidsp.platform.industry.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 行业分析历史列表项（简版，用于历史列表渲染）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryAnalysisHistoryItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String analysisId;
    private Long industryId;
    private long tookMs;
    private LocalDateTime createdAt;
    /** 6 维度标题拼接的简述："概况+市场+产业链+龙头+趋势+风险"。 */
    private String snippet;
}
