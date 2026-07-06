package com.aidsp.platform.company.api;

import com.aidsp.platform.core.api.PageResponse;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 公司 VO（视图对象）。
 * <p>作为公司相关接口的响应体（列表 / 详情 / 创建 / 更新后的返回值）。
 */
@Data
public class CompanyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String uscc;
    private Long industryId;
    private String industryName;
    private String mainBusiness;
    private String address;
    private LocalDate establishedAt;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
