package com.aidsp.platform.company.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公司实体（MyBatis-Plus Entity，对应 {@code company} 表）。
 * <p>财务核心指标扁平化为 {@code revenue / profit / financialPeriod} 三列，
 * <br>在 Service / Mapper 层组装为 {@code CompanyFinancialVO} 返回。
 * <p>业务字段映射：
 * <ul>
 *     <li>{@code industryId}     → column {@code industry_id}</li>
 *     <li>{@code industryName}   → column {@code industry_name}（冗余自 industry 表）</li>
 *     <li>{@code establishedAt}  → column {@code founded_at}</li>
 *     <li>{@code business}       → column {@code business}（JSON，由 {@link JacksonTypeHandler} 序列化）</li>
 *     <li>{@code createdAt}      → 自动填充（INSERT）</li>
 *     <li>{@code updatedAt}      → 自动填充（INSERT / UPDATE）</li>
 *     <li>{@code status}         → 软删除（{@link TableLogic}）</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "company", autoResultMap = true)
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 股票代码（如 002837）。 */
    private String code;

    private String uscc;

    @TableField("industry_id")
    private Long industryId;

    /** 行业名称（冗余字段，Service 层根据 industryId 写入）。 */
    @TableField("industry_name")
    private String industryName;

    /** 细分行业（如 "液冷设备"）。 */
    private String industry;

    @TableField("main_business")
    private String mainBusiness;

    /** 业务板块列表（如 ["机房温控", "液冷散热"]），以 JSON 形式持久化。 */
    @TableField(value = "business", typeHandler = JacksonTypeHandler.class)
    private List<String> business;

    private String address;

    @TableField("founded_at")
    private LocalDate establishedAt;

    private String description;

    /** 营收（元）。 */
    private BigDecimal revenue;

    /** 净利润（元）。 */
    private BigDecimal profit;

    /** 财报周期（如 2024 / 2025Q1）。 */
    @TableField("financial_period")
    private String financialPeriod;

    /** 软删除标记：0-启用 1-禁用。 */
    @TableLogic
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
