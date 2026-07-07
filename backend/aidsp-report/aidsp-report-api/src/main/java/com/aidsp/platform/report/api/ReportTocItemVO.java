package com.aidsp.platform.report.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报告目录（TOC）条目。
 * <p>用于前端左侧目录导航，点击跳转到对应锚点。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTocItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 锚点 ID（与正文中标题元素的 id 对应）。 */
    private String anchor;

    /** 标题文本。 */
    private String title;

    /** 层级：1=H1, 2=H2, 3=H3。 */
    private Integer level;
}
