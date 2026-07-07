package com.aidsp.platform.industry.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 产业链节点（上游 / 中游 / 下游）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryChainNodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 节点名称（如：上游-原材料、中游-制造、下游-应用）。 */
    private String name;

    /** 节点类型：UPSTREAM / MIDSTREAM / DOWNSTREAM。 */
    private String type;

    /** 节点描述。 */
    private String description;

    /** 节点代表企业或细分领域（多条用斜杠分隔）。 */
    private String representatives;
}
