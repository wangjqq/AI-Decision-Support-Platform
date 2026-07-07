package com.aidsp.platform.company.repository;

import com.aidsp.platform.company.entity.Company;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 公司样例数据。
 * <p>启动时通过 {@link CompanyDataProvider} 注入"真实企业"数据集到 {@code company} 表。
 * <p>已存在则跳过（按 {@code code} 唯一键判断），保证重复启动幂等。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanySeedData {

    private final CompanyMapper companyMapper;

    @PostConstruct
    public void seed() {
        List<Company> seedList = CompanyDataProvider.defaultCompanies();
        int inserted = 0;
        for (Company c : seedList) {
            // 幂等：code 已存在则跳过（股票代码唯一）
            if (c.getCode() != null && existsByCode(c.getCode())) {
                log.debug("[CompanySeedData] skip existing company code={}", c.getCode());
                continue;
            }
            companyMapper.insert(c);
            inserted++;
            log.info("[CompanySeedData] inserted company id={}, code={}, name={}",
                    c.getId(), c.getCode(), c.getName());
        }
        log.info("[CompanySeedData] seed done, inserted={}, total={}", inserted, countAll());
    }

    private boolean existsByCode(String code) {
        return companyMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Company>()
                        .eq(Company::getCode, code)) > 0;
    }

    private long countAll() {
        return companyMapper.selectCount(null);
    }
}
