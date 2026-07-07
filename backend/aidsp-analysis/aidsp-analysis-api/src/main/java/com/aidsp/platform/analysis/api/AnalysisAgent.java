package com.aidsp.platform.analysis.api;

/**
 * 领域分析 Agent 抽象。
 * <p>每种分析意图对应一个具体 Agent。
 */
public interface AnalysisAgent {

    /**
     * 声明本 Agent 支持的分析类型。
     * <p>返回 null 表示该 Agent 兜底支持所有类型（用于 Mock / 占位实现）。
     *
     * @return 支持的分析类型，null 表示全量支持
     */
    AnalysisType supports();

    /**
     * 执行分析并返回结果。
     *
     * @param request 分析请求
     * @return 分析结果
     */
    AnalysisResultDTO run(AnalysisQueryRequest request);
}
