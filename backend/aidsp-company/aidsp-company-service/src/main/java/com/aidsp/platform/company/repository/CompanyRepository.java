package com.aidsp.platform.company.repository;

import com.aidsp.platform.company.entity.Company;
import com.aidsp.platform.core.api.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 公司内存仓储（MVP 阶段使用 ConcurrentHashMap）。
 * <p>所有方法线程安全，可直接被 Service 注入。
 */
@Slf4j
@Repository
public class CompanyRepository {

    private final Map<Long, Company> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(0L);

    /**
     * 生成下一个公司 ID（雪花自增模拟）。
     */
    public Long nextId() {
        return idGen.incrementAndGet();
    }

    /**
     * 保存新公司（不指定 id 时自动分配）。
     */
    public Company save(Company company) {
        if (company.getId() == null) {
            company.setId(nextId());
        }
        store.put(company.getId(), company);
        return company;
    }

    /**
     * 更新公司。
     */
    public Optional<Company> update(Company company) {
        if (company.getId() == null) {
            return Optional.empty();
        }
        Company previous = store.get(company.getId());
        if (previous == null) {
            return Optional.empty();
        }
        store.put(company.getId(), company);
        return Optional.of(company);
    }

    /**
     * 按 ID 删除。
     */
    public boolean deleteById(Long id) {
        return store.remove(id) != null;
    }

    /**
     * 按 ID 查询。
     */
    public Optional<Company> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    /**
     * 公司名是否已存在（排除自身 ID）。
     */
    public boolean existsByName(String name, Long excludeId) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return store.values().stream().anyMatch(c ->
                c.getName() != null
                        && c.getName().equals(name)
                        && !c.getId().equals(excludeId));
    }

    /**
     * USCC 是否已存在（排除自身 ID）。
     */
    public boolean existsByUscc(String uscc, Long excludeId) {
        if (uscc == null || uscc.isBlank()) {
            return false;
        }
        return store.values().stream().anyMatch(c ->
                c.getUscc() != null
                        && c.getUscc().equals(uscc)
                        && !c.getId().equals(excludeId));
    }

    /**
     * 分页查询。
     *
     * @param keyword    关键字（公司名模糊匹配，不区分大小写）
     * @param industryId 行业 ID（精确匹配，null 表示不过滤）
     */
    public PageResponse<Company> page(Long page, Long size, String keyword, Long industryId) {
        long p = page == null || page <= 0 ? 1L : page;
        long s = size == null || size <= 0 ? 20L : size;
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        List<Company> filtered = store.values().stream()
                .filter(c -> industryId == null || industryId.equals(c.getIndustryId()))
                .filter(c -> kw.isEmpty() || (c.getName() != null && c.getName().toLowerCase(Locale.ROOT).contains(kw)))
                .sorted(Comparator.comparing(Company::getId))
                .collect(Collectors.toList());

        long total = filtered.size();
        long pages = (total + s - 1) / s;
        long from = Math.min((p - 1) * s, total);
        long to = Math.min(from + s, total);
        List<Company> pageList = filtered.subList((int) from, (int) to);

        return PageResponse.<Company>builder()
                .list(pageList)
                .total(total)
                .page(p)
                .size(s)
                .pages(pages)
                .build();
    }

    /**
     * 直接写入（供 SeedData 初始化用，不分配新 id）。
     */
    public void putIfAbsent(Company company) {
        if (company.getId() == null) {
            company.setId(nextId());
        }
        store.putIfAbsent(company.getId(), company);
        log.info("[CompanyRepository] seed company id={}, name={}", company.getId(), company.getName());
    }

    /**
     * 批量覆盖写入（供 DataProvider 注入真实企业数据）。
     * <p>同步将自增 id 游标推至当前最大 id，避免后续新增 id 冲突。
     */
    public void putAll(java.util.Collection<Company> companies) {
        if (companies == null || companies.isEmpty()) {
            return;
        }
        for (Company c : companies) {
            if (c.getId() == null) {
                c.setId(nextId());
            }
            store.put(c.getId(), c);
        }
        // 推动 idGen 至当前最大 id
        long maxId = companies.stream()
                .map(Company::getId)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        idGen.updateAndGet(prev -> Math.max(prev, maxId));
        log.info("[CompanyRepository] putAll {} companies, idGen={}", companies.size(), idGen.get());
    }
}
