package com.aidsp.platform.company.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 公司分析的引用参考。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAnalysisReferenceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;
    private String url;
    private String snippet;
}
