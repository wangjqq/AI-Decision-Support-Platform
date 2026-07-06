package com.aidsp.platform.analysis.api;

/**
 * 分析意图类型。
 * <p>用于意图识别后路由到对应的领域 Agent。
 */
public enum AnalysisType {
    /** 公司维度分析。 */
    COMPANY,
    /** 行业维度分析。 */
    INDUSTRY,
    /** 研报 / 报告维度分析。 */
    REPORT
}
