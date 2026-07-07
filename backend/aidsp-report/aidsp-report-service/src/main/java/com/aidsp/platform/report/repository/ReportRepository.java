package com.aidsp.platform.report.repository;

import com.aidsp.platform.core.api.PageResponse;
import com.aidsp.platform.report.api.ReportHistoryItemVO;
import com.aidsp.platform.report.entity.Report;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 报告内存仓储（MVP 阶段使用 ConcurrentHashMap）。
 * <p>key = reportId；按 companyId 倒序索引便于历史查询。
 */
@Slf4j
@Repository
public class ReportRepository {

    private final Map<String, Report> store = new ConcurrentHashMap<>();
    /** reportId 列表（按 put 时间倒序插入头部）。 */
    private final List<String> index = new java.util.concurrent.CopyOnWriteArrayList<>();

    public Report save(Report report) {
        store.put(report.getReportId(), report);
        // 头部插入：新的报告在最前面
        if (!report.getReportId().equals(index.isEmpty() ? null : index.get(0))) {
            index.remove(report.getReportId());
            index.add(0, report.getReportId());
        }
        log.info("[ReportRepository] saved reportId={}, companyId={}",
                report.getReportId(), report.getCompanyId());
        return report;
    }

    public Optional<Report> findById(String reportId) {
        if (reportId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(reportId));
    }

    public boolean deleteById(String reportId) {
        Report removed = store.remove(reportId);
        if (removed == null) {
            return false;
        }
        index.remove(reportId);
        return true;
    }

    /**
     * 分页查询报告历史（按 createdAt 倒序，可选关键字 / 公司 ID 过滤）。
     */
    public PageResponse<ReportHistoryItemVO> page(String keyword, Long companyId, Long page, Long size) {
        long p = page == null || page <= 0 ? 1L : page;
        long s = size == null || size <= 0 ? 20L : size;
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        List<Report> filtered = index.stream()
                .map(store::get)
                .filter(java.util.Objects::nonNull)
                .filter(r -> companyId == null || companyId.equals(r.getCompanyId()))
                .filter(r -> kw.isEmpty()
                        || (r.getTitle() != null && r.getTitle().toLowerCase(Locale.ROOT).contains(kw))
                        || (r.getCompanyName() != null && r.getCompanyName().toLowerCase(Locale.ROOT).contains(kw))
                        || (r.getIndustryName() != null && r.getIndustryName().toLowerCase(Locale.ROOT).contains(kw)))
                .sorted(Comparator.comparing(Report::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        long total = filtered.size();
        long pages = (total + s - 1) / s;
        long from = Math.min((p - 1) * s, total);
        long to = Math.min(from + s, total);
        List<ReportHistoryItemVO> pageList = filtered.subList((int) from, (int) to).stream()
                .map(this::toHistoryItem)
                .toList();

        return PageResponse.<ReportHistoryItemVO>builder()
                .list(pageList)
                .total(total)
                .page(p)
                .size(s)
                .pages(pages)
                .build();
    }

    private ReportHistoryItemVO toHistoryItem(Report r) {
        return ReportHistoryItemVO.builder()
                .reportId(r.getReportId())
                .title(r.getTitle())
                .companyId(r.getCompanyId())
                .companyName(r.getCompanyName())
                .industryId(r.getIndustryId())
                .industryName(r.getIndustryName())
                .status(r.getStatus())
                .tookMs(r.getTookMs())
                .createdAt(r.getCreatedAt())
                .summary(r.getSummary())
                .build();
    }
}
