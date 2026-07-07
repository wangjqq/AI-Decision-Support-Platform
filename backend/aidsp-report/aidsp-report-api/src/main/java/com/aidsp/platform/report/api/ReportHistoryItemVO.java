package com.aidsp.platform.report.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报告历史列表项（用于报告列表页）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportHistoryItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 报告 ID。 */
    private String reportId;

    /** 报告标题。 */
    private String title;

    /** 公司 ID。 */
    private Long companyId;

    /** 公司名。 */
    private String companyName;

    /** 行业 ID（可选）。 */
    private Long industryId;

    /** 行业名（可选）。 */
    private String industryName;

    /** 报告状态：PENDING / RUNNING / SUCCESS / FAILED。 */
    private String status;

    /** 生成耗时。 */
    private long tookMs;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 报告摘要（120 字以内）。 */
    private String summary;
}
