package com.aidsp.platform.industry.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 行业龙头企业信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryLeadingCompanyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 企业名称。 */
    private String name;

    /** 股票代码（如有）。 */
    private String stockCode;

    /** 市场份额（百分比，0~100）。 */
    private Double marketShare;

    /** 核心标签（如：全球第一 / 龙头 / 黑马）。 */
    private String tag;

    /** 简短描述。 */
    private String description;
}
