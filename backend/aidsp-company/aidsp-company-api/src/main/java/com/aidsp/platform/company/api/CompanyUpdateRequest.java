package com.aidsp.platform.company.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 更新公司请求。
 */
@Data
public class CompanyUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 16)
    private String code;

    @NotBlank
    @Pattern(regexp = "^\\d{18}$", message = "统一社会信用代码必须为 18 位数字")
    private String uscc;

    @NotNull
    private Long industryId;

    @Size(max = 64)
    private String industry;

    @NotBlank
    @Size(max = 500)
    private String mainBusiness;

    private List<String> business;

    @Size(max = 200)
    private String address;

    private LocalDate establishedAt;

    @Size(max = 2000)
    private String description;

    private CompanyFinancialVO financial;
}
