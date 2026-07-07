package com.aidsp.platform.company.repository;

import com.aidsp.platform.company.entity.Company;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公司样例数据。
 * <p>启动时通过 {@link CompanyDataProvider} 注入"真实企业"数据集（含 A 股上市公司）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanySeedData {

    private final CompanyRepository repository;

    @PostConstruct
    public void seed() {
        LocalDateTime now = LocalDateTime.now();
        java.util.List<Company> seedList = CompanyDataProvider.defaultCompanies();
        for (Company c : seedList) {
            c.setCreatedAt(now);
            c.setUpdatedAt(now);
        }
        repository.putAll(seedList);
        log.info("[CompanySeedData] seeded {} companies", seedList.size());
    }
}
