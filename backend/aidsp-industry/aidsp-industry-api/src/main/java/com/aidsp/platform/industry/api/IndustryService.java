package com.aidsp.platform.industry.api;

import com.aidsp.platform.core.api.PageResponse;

/**
 * 行业模块 Dubbo 服务接口。
 * <p>Dubbo in-JVM 跨模块调用入口。
 */
public interface IndustryService {

    /**
     * 分页查询行业。
     */
    PageResponse<IndustryVO> page(Long page, Long size, String keyword, Integer level, Long parentId);

    /**
     * 按 ID 获取行业详情。
     */
    IndustryVO getById(Long id);

    /**
     * 创建行业。
     */
    IndustryVO create(IndustryCreateRequest request);

    /**
     * 更新行业。
     */
    IndustryVO update(Long id, IndustryUpdateRequest request);

    /**
     * 删除行业。
     */
    void delete(Long id);
}
