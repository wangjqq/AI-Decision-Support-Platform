package com.aidsp.platform.industry.repository;

import com.aidsp.platform.industry.api.IndustryAnalysisHistoryItemVO;
import com.aidsp.platform.industry.entity.IndustryAnalysisResult;
import com.aidsp.platform.core.api.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 行业分析结果内存仓储。
 * <p>key = analysisId；按 industryId 倒序索引便于历史查询。
 */
@Slf4j
@Repository
public class IndustryAnalysisRepository {

    private final Map<String, IndustryAnalysisResult> store = new ConcurrentHashMap<>();
    /** industryId → analysisId 列表（按 put 时间倒序插入头部） */
    private final Map<Long, java.util.Deque<String>> index = new ConcurrentHashMap<>();

    public IndustryAnalysisResult save(IndustryAnalysisResult result) {
        store.put(result.getAnalysisId(), result);
        index.computeIfAbsent(result.getIndustryId(), k -> new java.util.concurrent.ConcurrentLinkedDeque<>())
                .addFirst(result.getAnalysisId());
        log.info("[IndustryAnalysisRepository] saved analysisId={}, industryId={}",
                result.getAnalysisId(), result.getIndustryId());
        return result;
    }

    public Optional<IndustryAnalysisResult> findById(String analysisId) {
        if (analysisId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(analysisId));
    }

    public boolean deleteById(String analysisId) {
        IndustryAnalysisResult removed = store.remove(analysisId);
        if (removed == null) {
            return false;
        }
        java.util.Deque<String> ids = index.get(removed.getIndustryId());
        if (ids != null) {
            ids.remove(analysisId);
        }
        return true;
    }

    /**
     * 分页查询某行业的分析历史（按 createdAt 倒序）。
     */
    public PageResponse<IndustryAnalysisHistoryItemVO> pageByIndustry(Long industryId, Long page, Long size) {
        long p = page == null || page <= 0 ? 1L : page;
        long s = size == null || size <= 0 ? 20L : size;
        java.util.Deque<String> ids = index.getOrDefault(industryId, new java.util.concurrent.ConcurrentLinkedDeque<>());
        List<IndustryAnalysisHistoryItemVO> items = ids.stream()
                .map(store::get)
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(IndustryAnalysisResult::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> IndustryAnalysisHistoryItemVO.builder()
                        .analysisId(r.getAnalysisId())
                        .industryId(r.getIndustryId())
                        .tookMs(r.getTookMs())
                        .createdAt(r.getCreatedAt())
                        .snippet(buildSnippet(r))
                        .build())
                .collect(java.util.stream.Collectors.toList());
        long total = items.size();
        long pages = (total + s - 1) / s;
        long from = Math.min((p - 1) * s, total);
        long to = Math.min(from + s, total);
        List<IndustryAnalysisHistoryItemVO> pageList = items.subList((int) from, (int) to);
        return PageResponse.<IndustryAnalysisHistoryItemVO>builder()
                .list(pageList)
                .total(total)
                .page(p)
                .size(s)
                .pages(pages)
                .build();
    }

    private String buildSnippet(IndustryAnalysisResult r) {
        StringBuilder sb = new StringBuilder();
        appendDim(sb, r.getOverview());
        appendDim(sb, r.getMarketSize());
        appendDim(sb, r.getChain());
        appendDim(sb, r.getLeading());
        appendDim(sb, r.getTrends());
        appendDim(sb, r.getRisks());
        return sb.toString();
    }

    private void appendDim(StringBuilder sb, com.aidsp.platform.industry.api.IndustryDimensionVO d) {
        if (d == null || d.getTitle() == null) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" + ");
        }
        sb.append(d.getTitle());
    }
}
