package com.aidsp.platform.company.entity;

import com.aidsp.platform.company.api.CompanyVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 公司实体（内存版，MVP 阶段不接 DB）。
 * <p>字段与 {@link CompanyVO} 1:1 映射，Entity ↔ VO 转换由 Mapper 完成。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company implements Serializable {

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
