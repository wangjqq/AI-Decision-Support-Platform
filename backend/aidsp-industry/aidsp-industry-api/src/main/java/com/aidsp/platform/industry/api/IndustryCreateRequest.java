package com.aidsp.platform.industry.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建行业请求。
 */
@Data
public class IndustryCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 32)
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "行业编码仅允许字母、数字、下划线")
    private String code;

    @NotBlank
    @Size(max = 128)
    private String name;

    @NotNull
    @Min(value = 1, message = "level 范围 1-4")
    @Max(value = 4, message = "level 范围 1-4")
    private Integer level;

    private Long parentId;

    @Size(max = 2000)
    private String description;

    @Size(max = 255)
    private String tags;
}
