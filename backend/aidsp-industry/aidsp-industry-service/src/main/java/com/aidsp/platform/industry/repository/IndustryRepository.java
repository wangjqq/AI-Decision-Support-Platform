package com.aidsp.platform.industry.repository;

import com.aidsp.platform.core.api.PageResponse;
import com.aidsp.platform.industry.entity.Industry;
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
 * 行业内存仓储（MVP 阶段使用 ConcurrentHashMap）。
 * <p>所有方法线程安全，可直接被 Service 注入。
 */
@Slf4j
@Repository
public class IndustryRepository {

    private final Map<Long, Industry> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(0L);

    /**
     * 生成下一个行业 ID。
     */
    public Long nextId() {
        return idGen.incrementAndGet();
    }

    /**
     * 保存新行业（不指定 id 时自动分配）。
     */
    public Industry save(Industry industry) {
        if (industry.getId() == null) {
            industry.setId(nextId());
        }
        store.put(industry.getId(), industry);
        return industry;
    }

    /**
     * 更新行业。
     */
    public Optional<Industry> update(Industry industry) {
        if (industry.getId() == null) {
            return Optional.empty();
        }
        Industry previous = store.get(industry.getId());
        if (previous == null) {
            return Optional.empty();
        }
        store.put(industry.getId(), industry);
        return Optional.of(industry);
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
    public Optional<Industry> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    /**
     * 按编码查询（不区分大小写）。
     */
    public Optional<Industry> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return store.values().stream()
                .filter(c -> c.getCode() != null && c.getCode().equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * 行业名是否已存在（排除自身 ID）。
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
     * 行业编码是否已存在（排除自身 ID）。
     */
    public boolean existsByCode(String code, Long excludeId) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return store.values().stream().anyMatch(c ->
                c.getCode() != null
                        && c.getCode().equalsIgnoreCase(code)
                        && !c.getId().equals(excludeId));
    }

    /**
     * 是否有子行业。
     */
    public boolean hasChildren(Long parentId) {
        if (parentId == null) {
            return false;
        }
        return store.values().stream().anyMatch(c -> parentId.equals(c.getParentId()));
    }

    /**
     * 分页查询。
     *
     * @param keyword  关键字（行业名 / 编码模糊匹配，不区分大小写）
     * @param level    层级（1-门类 2-大类 3-中类 4-小类），null 表示不过滤
     * @param parentId 父行业 ID（精确匹配，null 表示不过滤）
     */
    public PageResponse<Industry> page(Long page, Long size, String keyword, Integer level, Long parentId) {
        long p = page == null || page <= 0 ? 1L : page;
        long s = size == null || size <= 0 ? 20L : size;
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        List<Industry> filtered = store.values().stream()
                .filter(c -> level == null || level.equals(c.getLevel()))
                .filter(c -> parentId == null || parentId.equals(c.getParentId()))
                .filter(c -> {
                    if (kw.isEmpty()) return true;
                    String n = c.getName() == null ? "" : c.getName().toLowerCase(Locale.ROOT);
                    String cd = c.getCode() == null ? "" : c.getCode().toLowerCase(Locale.ROOT);
                    return n.contains(kw) || cd.contains(kw);
                })
                .sorted(Comparator.comparing(Industry::getId))
                .collect(Collectors.toList());

        long total = filtered.size();
        long pages = (total + s - 1) / s;
        long from = Math.min((p - 1) * s, total);
        long to = Math.min(from + s, total);
        List<Industry> pageList = filtered.subList((int) from, (int) to);

        return PageResponse.<Industry>builder()
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
    public void putIfAbsent(Industry industry) {
        if (industry.getId() == null) {
            industry.setId(nextId());
        }
        store.putIfAbsent(industry.getId(), industry);
        log.info("[IndustryRepository] seed industry id={}, code={}, name={}",
                industry.getId(), industry.getCode(), industry.getName());
    }

    /**
     * 当前已写入的行业数量。
     */
    public int count() {
        return store.size();
    }
}
