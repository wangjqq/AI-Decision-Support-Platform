package com.aidsp.platform.analysis.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 智能分析结果 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 本次查询唯一 ID（q-yyyyMMdd-xxxxxxxxxxxx）。 */
    private String queryId;

    /** 命中的意图类型。 */
    private AnalysisType analysisType;

    /** 分析结果内容（summary / keyPoints / metrics / references）。 */
    private Map<String, Object> result;

    /** 总耗时（毫秒）。 */
    private long tookMs;
}
