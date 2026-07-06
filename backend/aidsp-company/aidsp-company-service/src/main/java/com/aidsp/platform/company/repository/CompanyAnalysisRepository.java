package com.aidsp.platform.company.repository;

import com.aidsp.platform.company.api.CompanyAnalysisHistoryItemVO;
import com.aidsp.platform.company.entity.CompanyAnalysisResult;
import com.aidsp.platform.core.api.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 公司分析结果内存仓储。
 * <p>key = analysisId；按 companyId 倒序索引便于历史查询。
 */
@Slf4j
@Repository
public class CompanyAnalysisRepository {

    private final Map<String, CompanyAnalysisResult> store = new ConcurrentHashMap<>();
    /** companyId → analysisId 列表（按 put 时间倒序插入头部） */
    private final Map<Long, java.util.Deque<String>> index = new ConcurrentHashMap<>();

    public CompanyAnalysisResult save(CompanyAnalysisResult result) {
        store.put(result.getAnalysisId(), result);
        index.computeIfAbsent(result.getCompanyId(), k -> new java.util.concurrent.ConcurrentLinkedDeque<>())
                .addFirst(result.getAnalysisId());
        log.info("[CompanyAnalysisRepository] saved analysisId={}, companyId={}",
                result.getAnalysisId(), result.getCompanyId());
        return result;
    }

    public Optional<CompanyAnalysisResult> findById(String analysisId) {
        if (analysisId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(analysisId));
    }

    public boolean deleteById(String analysisId) {
        CompanyAnalysisResult removed = store.remove(analysisId);
        if (removed == null) {
            return false;
        }
        java.util.Deque<String> ids = index.get(removed.getCompanyId());
        if (ids != null) {
            ids.remove(analysisId);
        }
        return true;
    }

    /**
     * 分页查询某公司的分析历史（按 createdAt 倒序）。
     */
    public PageResponse<CompanyAnalysisHistoryItemVO> pageByCompany(Long companyId, Long page, Long size) {
        long p = page == null || page <= 0 ? 1L : page;
        long s = size == null || size <= 0 ? 20L : size;
        java.util.Deque<String> ids = index.getOrDefault(companyId, new java.util.concurrent.ConcurrentLinkedDeque<>());
        List<CompanyAnalysisHistoryItemVO> items = ids.stream()
                .map(store::get)
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(CompanyAnalysisResult::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> CompanyAnalysisHistoryItemVO.builder()
                        .analysisId(r.getAnalysisId())
                        .companyId(r.getCompanyId())
                        .tookMs(r.getTookMs())
                        .createdAt(r.getCreatedAt())
                        .snippet(buildSnippet(r))
                        .build())
                .collect(java.util.stream.Collectors.toList());
        long total = items.size();
        long pages = (total + s - 1) / s;
        long from = Math.min((p - 1) * s, total);
        long to = Math.min(from + s, total);
        List<CompanyAnalysisHistoryItemVO> pageList = items.subList((int) from, (int) to);
        return PageResponse.<CompanyAnalysisHistoryItemVO>builder()
                .list(pageList)
                .total(total)
                .page(p)
                .size(s)
                .pages(pages)
                .build();
    }

    private String buildSnippet(CompanyAnalysisResult r) {
        StringBuilder sb = new StringBuilder();
        appendDim(sb, r.getOverview());
        appendDim(sb, r.getMainBusiness());
        appendDim(sb, r.getAdvantages());
        appendDim(sb, r.getRisks());
        appendDim(sb, r.getAiConclusion());
        return sb.toString();
    }

    private void appendDim(StringBuilder sb, com.aidsp.platform.company.api.CompanyDimensionVO d) {
        if (d == null || d.getTitle() == null) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" + ");
        }
        sb.append(d.getTitle());
    }
}
