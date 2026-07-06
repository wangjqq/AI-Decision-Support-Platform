package com.aidsp.platform.analysis.api;

/**
 * 智能分析 Dubbo 服务接口。
 */
public interface AnalysisService {

    /**
     * 根据用户问题进行意图识别、调度与结果整合。
     *
     * @param request 分析请求
     * @return 分析结果
     */
    AnalysisResultDTO analyze(AnalysisQueryRequest request);
}
