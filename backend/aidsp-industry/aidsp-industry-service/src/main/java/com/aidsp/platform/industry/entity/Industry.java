package com.aidsp.platform.industry.entity;

import com.aidsp.platform.industry.api.IndustryVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 行业实体（内存版，MVP 阶段不接 DB）。
 * <p>字段与 {@link IndustryVO} 1:1 映射，Entity ↔ VO 转换由 Mapper 完成。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Industry implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String name;
    private Integer level;
    private Long parentId;
    /** 父行业名称（缓存冗余）。 */
    private String parentName;
    private String description;
    private String tags;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
