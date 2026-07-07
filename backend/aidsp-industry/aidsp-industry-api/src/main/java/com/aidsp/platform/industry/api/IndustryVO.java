package com.aidsp.platform.industry.api;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 行业 VO（视图对象）。
 * <p>作为行业相关接口的响应体（列表 / 详情 / 创建 / 更新后的返回值）。
 */
@Data
public class IndustryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    /** 行业编码（GB/T 4754）。 */
    private String code;
    /** 行业名称。 */
    private String name;
    /** 层级：1-门类 2-大类 3-中类 4-小类。 */
    private Integer level;
    /** 父行业 ID，自引用 industry.id。 */
    private Long parentId;
    /** 父行业名称（冗余，便于前端展示）。 */
    private String parentName;
    /** 行业描述。 */
    private String description;
    /** 行业标签（逗号分隔）。 */
    private String tags;
    /** 行业状态：0-禁用 1-启用。 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
