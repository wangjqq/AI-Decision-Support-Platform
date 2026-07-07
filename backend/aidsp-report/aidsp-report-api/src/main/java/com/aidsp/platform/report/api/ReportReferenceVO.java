package com.aidsp.platform.report.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报告中的引用参考（行业研报 / 公司公告 / 政策文件等）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportReferenceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 引用标题。 */
    private String title;

    /** 链接地址（可选）。 */
    private String url;

    /** 引用摘要。 */
    private String snippet;

    /** 来源类型：INDUSTRY（行业）/ COMPANY（公司）/ POLICY（政策）/ NEWS（新闻）。 */
    private String sourceType;
}
