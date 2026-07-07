package com.aidsp.platform.company.entity;

import com.aidsp.platform.company.api.CompanyFinancialVO;
import com.aidsp.platform.company.api.CompanyVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公司实体（内存版，MVP 阶段不接 DB）。
 * <p>字段与 {@link CompanyVO} 1:1 映射，Entity ↔ VO 转换由 Mapper 完成。
 * <p>真实企业数据（股票代码、业务板块、财务指标）通过 {@code CompanyDataProvider} 注入。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    /** 股票代码（如 002837）。 */
    private String code;
    private String uscc;
    private Long industryId;
    private String industryName;
    /** 公司细分行业（如 "液冷设备"）。 */
    private String industry;
    private String mainBusiness;
    /** 业务板块列表（如 ["机房温控", "液冷散热"]）。 */
    private List<String> business;
    private String address;
    private LocalDate establishedAt;
    private String description;
    /** 财务核心指标。 */
    private CompanyFinancialVO financial;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
