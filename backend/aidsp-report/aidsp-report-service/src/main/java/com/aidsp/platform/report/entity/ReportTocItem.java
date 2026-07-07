package com.aidsp.platform.report.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报告目录条目（实体层）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTocItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String anchor;
    private String title;
    private Integer level;
}
