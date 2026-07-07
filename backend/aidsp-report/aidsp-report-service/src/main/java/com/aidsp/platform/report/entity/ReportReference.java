package com.aidsp.platform.report.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报告中的引用参考。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportReference implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;
    private String url;
    private String snippet;
    private String sourceType;
}
