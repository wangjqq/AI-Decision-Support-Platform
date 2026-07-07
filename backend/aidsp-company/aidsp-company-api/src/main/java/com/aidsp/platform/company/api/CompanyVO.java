package com.aidsp.platform.company.api;

import com.aidsp.platform.core.api.PageResponse;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公司 VO（视图对象）。
 * <p>作为公司相关接口的响应体（列表 / 详情 / 创建 / 更新后的返回值）。
 */
@Data
public class CompanyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    /** 股票代码（如 002837），非上市公司可为空。 */
    private String code;
    private String uscc;
    private Long industryId;
    private String industryName;
    /** 公司所属细分行业（如 "液冷设备"），用于详情页快速定位。 */
    private String industry;
    private String mainBusiness;
    /** 业务板块列表（如 ["机房温控", "液冷散热"]）。 */
    private List<String> business;
    private String address;
    private LocalDate establishedAt;
    private String description;
    /** 财务核心指标（营收/净利润/报告期）。 */
    private CompanyFinancialVO financial;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
