package com.aidsp.platform.company.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 公司财务核心指标 VO。
 * <p>用于详情接口中嵌套在 {@link CompanyVO#financial} 字段下。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyFinancialVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 营收（单位：元）。 */
    private BigDecimal revenue;

    /** 净利润（单位：元）。 */
    private BigDecimal profit;

    /** 报告期，如 2024 / 2025Q1。 */
    private String period;
}
