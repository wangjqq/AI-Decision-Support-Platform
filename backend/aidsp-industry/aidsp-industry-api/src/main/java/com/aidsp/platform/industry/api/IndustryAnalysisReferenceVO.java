package com.aidsp.platform.industry.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 行业分析的引用参考。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryAnalysisReferenceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;
    private String url;
    private String snippet;
}
