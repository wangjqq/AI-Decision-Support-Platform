package com.aidsp.platform.report.api;

import com.aidsp.platform.core.api.PageResponse;

/**
 * 报告模块 Dubbo 服务接口。
 * <p>跨模块调用入口（Search / Dashboard 等模块可通过 Dubbo 拉取报告数据）。
 * <p>本服务本身既作为 Dubbo 服务（{@code @DubboService}）暴露，
 * <br>也由同进程内的 {@code ReportController} 通过 Spring 直接注入使用。
 */
public interface ReportService {

    /**
     * 按 ID 查询报告详情。
     *
     * @param reportId 报告 ID
     * @return 报告 VO；不存在返回 null
     */
    ReportVO getById(String reportId);

    /**
     * 分页查询报告历史。
     *
     * @param keyword  关键字（按标题模糊匹配，可空）
     * @param companyId 公司 ID 过滤（可空）
     * @param page      页码（1-based）
     * @param size      每页大小
     * @return 分页响应
     */
    PageResponse<ReportHistoryItemVO> page(String keyword, Long companyId, Long page, Long size);
}
