package com.aidsp.platform.report.service;

import com.aidsp.platform.analysis.util.NanoId;
import com.aidsp.platform.company.api.CompanyAnalysisResultVO;
import com.aidsp.platform.industry.api.IndustryAnalysisResultVO;
import com.aidsp.platform.core.api.PageResponse;
import com.aidsp.platform.core.exception.BusinessException;
import com.aidsp.platform.core.exception.ErrorCode;
import com.aidsp.platform.report.agent.ReportAgent;
import com.aidsp.platform.report.api.ReportGenerateRequest;
import com.aidsp.platform.report.api.ReportHistoryItemVO;
import com.aidsp.platform.report.api.ReportReferenceVO;
import com.aidsp.platform.report.api.ReportSectionVO;
import com.aidsp.platform.report.api.ReportService;
import com.aidsp.platform.report.api.ReportTocItemVO;
import com.aidsp.platform.report.api.ReportVO;
import com.aidsp.platform.report.entity.Report;
import com.aidsp.platform.report.entity.ReportReference;
import com.aidsp.platform.report.entity.ReportSection;
import com.aidsp.platform.report.entity.ReportTocItem;
import com.aidsp.platform.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 报告业务服务实现。
 * <p>页面直驱：接收 {@link ReportGenerateRequest}（含公司 / 行业分析结果），
 * <br>调用 {@link ReportAgent} 生成 Markdown 报告（由 Spring 条件注入 Mock / LLM 实现），
 * <br>包装为 {@link ReportVO} 并落 {@link ReportRepository}。
 * <p>同时作为 Dubbo 服务（{@code @DubboService}）暴露 {@link ReportService#getById(String)}
 * <br>与 {@link ReportService#page(String, Long, Long, Long)}，供 Search / Dashboard 等模块调用。
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int SUMMARY_MAX = 120;

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final ReportAgent agent;

    // ===================== 报告生成 =====================

    /**
     * 触发一次报告生成（非 Dubbo 暴露，仅本地 Service 入口）。
     */
    public ReportVO generate(ReportGenerateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "请求体不能为空");
        }
        if (request.getCompanyId() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "companyId 不能为空");
        }
        if (request.getCompanyAnalysisId() == null || request.getCompanyAnalysisId().isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "companyAnalysisId 不能为空");
        }
        if (request.getCompanyAnalysis() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "companyAnalysis 不能为空");
        }
        // 行业分析为可选；但若提供 industryAnalysisId 则 industryAnalysis 必填
        if (request.getIndustryAnalysisId() != null && !request.getIndustryAnalysisId().isBlank()
                && request.getIndustryAnalysis() == null) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "industryAnalysisId 与 industryAnalysis 必须同时提供");
        }

        CompanyAnalysisResultVO company = request.getCompanyAnalysis();
        IndustryAnalysisResultVO industry = request.getIndustryAnalysis();

        String companyName = nonBlank(request.getCompanyName(),
                company.getCompanyName(), "目标公司");
        String industryName = nonBlank(request.getIndustryName(),
                industry == null ? null : industry.getIndustryName(), "相关行业");
        String reportType = industry == null ? "COMPANY" : "COMPREHENSIVE";

        log.info("[ReportServiceImpl] generating report, companyId={}, companyAnalysisId={}, industryId={}, industryAnalysisId={}",
                request.getCompanyId(), request.getCompanyAnalysisId(),
                request.getIndustryId(), request.getIndustryAnalysisId());

        // 调 Agent 生成
        ReportAgent.ReportAgentResult raw = agent.generate(
                new ReportAgent.ReportAgentRequest(
                        request.getTitle(),
                        request.getQuery(),
                        company,
                        industry
                )
        );

        // 组装实体
        LocalDateTime now = LocalDateTime.now();
        Report report = Report.builder()
                .reportId(buildReportId())
                .title(raw.title())
                .type(reportType)
                .status("SUCCESS")
                .companyId(request.getCompanyId())
                .companyName(companyName)
                .industryId(request.getIndustryId())
                .industryName(industry == null ? null : industryName)
                .companyAnalysisId(request.getCompanyAnalysisId())
                .industryAnalysisId(request.getIndustryAnalysisId())
                .summary(truncate(raw.summary(), SUMMARY_MAX))
                .summaryMarkdown(raw.summaryMarkdown())
                .toc(toTocEntity(raw.toc()))
                .sections(toSectionEntity(raw.sections()))
                .markdown(raw.markdown())
                .references(toReferenceEntity(raw.references()))
                .tookMs(raw.tookMs())
                .createdAt(now)
                .updatedAt(now)
                .build();

        reportRepository.save(report);
        log.info("[ReportServiceImpl] report generated, reportId={}, company='{}', industry='{}', tookMs={}",
                report.getReportId(), companyName, industryName, report.getTookMs());

        return reportMapper.toVO(report);
    }

    // ===================== 查询 =====================

    @Override
    public ReportVO getById(String reportId) {
        if (reportId == null || reportId.isBlank()) {
            return null;
        }
        return reportRepository.findById(reportId)
                .map(reportMapper::toVO)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
    }

    @Override
    public PageResponse<ReportHistoryItemVO> page(String keyword, Long companyId, Long page, Long size) {
        return reportRepository.page(keyword, companyId, page, size);
    }

    /**
     * 删除报告。
     */
    public boolean deleteById(String reportId) {
        if (reportId == null || reportId.isBlank()) {
            return false;
        }
        boolean removed = reportRepository.deleteById(reportId);
        if (!removed) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
        }
        return true;
    }

    // ===================== 实体转换 =====================

    private List<ReportTocItem> toTocEntity(List<ReportTocItemVO> src) {
        if (src == null) {
            return new ArrayList<>();
        }
        List<ReportTocItem> dst = new ArrayList<>(src.size());
        for (ReportTocItemVO v : src) {
            dst.add(ReportTocItem.builder()
                    .anchor(v.getAnchor())
                    .title(v.getTitle())
                    .level(v.getLevel())
                    .build());
        }
        return dst;
    }

    private List<ReportSection> toSectionEntity(List<ReportSectionVO> src) {
        if (src == null) {
            return new ArrayList<>();
        }
        List<ReportSection> dst = new ArrayList<>(src.size());
        for (ReportSectionVO v : src) {
            dst.add(ReportSection.builder()
                    .key(v.getKey())
                    .title(v.getTitle())
                    .anchor(v.getAnchor())
                    .markdown(v.getMarkdown())
                    .build());
        }
        return dst;
    }

    private List<ReportReference> toReferenceEntity(List<ReportReferenceVO> src) {
        if (src == null) {
            return new ArrayList<>();
        }
        List<ReportReference> dst = new ArrayList<>(src.size());
        for (ReportReferenceVO v : src) {
            dst.add(ReportReference.builder()
                    .title(v.getTitle())
                    .url(v.getUrl())
                    .snippet(v.getSnippet())
                    .sourceType(v.getSourceType())
                    .build());
        }
        return dst;
    }

    // ===================== 工具方法 =====================

    private String buildReportId() {
        return "rp-" + LocalDate.now().format(DATE_FMT) + "-" + NanoId.nanoId(12);
    }

    private String nonBlank(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isBlank()) {
                return c;
            }
        }
        return "";
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "…";
    }
}
