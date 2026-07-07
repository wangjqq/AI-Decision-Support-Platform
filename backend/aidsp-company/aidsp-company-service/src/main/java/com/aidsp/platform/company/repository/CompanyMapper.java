package com.aidsp.platform.company.repository;

import com.aidsp.platform.company.entity.Company;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公司 MyBatis-Plus Mapper。
 * <p>继承 {@link BaseMapper} 获得 CRUD 基础能力（selectById / selectPage / insert / updateById / deleteById 等）。
 * <p>复杂查询（按 industryId / 关键字搜索 / 软删除过滤）由 Service 层使用
 * {@code LambdaQueryWrapper} / {@code Page} 组合完成。
 */
@Mapper
public interface CompanyMapper extends BaseMapper<Company> {
}
