package com.aidsp.platform.company.api;

import com.aidsp.platform.core.api.PageResponse;

/**
 * 公司模块 Dubbo 服务接口。
 * <p>Dubbo in-JVM 跨模块调用入口。
 */
public interface CompanyService {

    /**
     * 分页查询公司。
     */
    PageResponse<CompanyVO> page(Long page, Long size, String keyword, Long industryId);

    /**
     * 按 ID 获取公司详情。
     */
    CompanyVO getById(Long id);

    /**
     * 创建公司，返回新建后的实体（带 id / createdAt / updatedAt）。
     */
    CompanyVO create(CompanyCreateRequest request);

    /**
     * 更新公司，返回更新后的实体。
     */
    CompanyVO update(Long id, CompanyUpdateRequest request);

    /**
     * 删除公司。
     */
    void delete(Long id);
}
