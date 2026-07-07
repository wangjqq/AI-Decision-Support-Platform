package com.aidsp.platform.report.service;

import com.aidsp.platform.report.api.ReportReferenceVO;
import com.aidsp.platform.report.api.ReportSectionVO;
import com.aidsp.platform.report.api.ReportTocItemVO;
import com.aidsp.platform.report.api.ReportVO;
import com.aidsp.platform.report.entity.Report;
import com.aidsp.platform.report.entity.ReportReference;
import com.aidsp.platform.report.entity.ReportSection;
import com.aidsp.platform.report.entity.ReportTocItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Entity ↔ VO 手动映射工具。
 * <p>MVP 阶段不引入 MapStruct，避免额外依赖。
 */
@Component
public class ReportMapper {

    public ReportVO toVO(Report r) {
        if (r == null) {
            return null;
        }
        ReportVO v = new ReportVO();
        v.setReportId(r.getReportId());
        v.setTitle(r.getTitle());
        v.setType(r.getType());
        v.setStatus(r.getStatus());
        v.setCompanyId(r.getCompanyId());
        v.setCompanyName(r.getCompanyName());
        v.setIndustryId(r.getIndustryId());
        v.setIndustryName(r.getIndustryName());
        v.setCompanyAnalysisId(r.getCompanyAnalysisId());
        v.setIndustryAnalysisId(r.getIndustryAnalysisId());
        v.setSummary(r.getSummary());
        v.setSummaryMarkdown(r.getSummaryMarkdown());
        v.setToc(toTocVO(r.getToc()));
        v.setSections(toSectionVO(r.getSections()));
        v.setMarkdown(r.getMarkdown());
        v.setReferences(toReferenceVO(r.getReferences()));
        v.setTookMs(r.getTookMs());
        v.setCreatedAt(r.getCreatedAt());
        v.setUpdatedAt(r.getUpdatedAt());
        return v;
    }

    private List<ReportTocItemVO> toTocVO(List<ReportTocItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(t -> ReportTocItemVO.builder()
                        .anchor(t.getAnchor())
                        .title(t.getTitle())
                        .level(t.getLevel())
                        .build())
                .toList();
    }

    private List<ReportSectionVO> toSectionVO(List<ReportSection> sections) {
        if (sections == null) {
            return List.of();
        }
        return sections.stream()
                .map(s -> ReportSectionVO.builder()
                        .key(s.getKey())
                        .title(s.getTitle())
                        .anchor(s.getAnchor())
                        .markdown(s.getMarkdown())
                        .build())
                .toList();
    }

    private List<ReportReferenceVO> toReferenceVO(List<ReportReference> refs) {
        if (refs == null) {
            return List.of();
        }
        return refs.stream()
                .map(r -> ReportReferenceVO.builder()
                        .title(r.getTitle())
                        .url(r.getUrl())
                        .snippet(r.getSnippet())
                        .sourceType(r.getSourceType())
                        .build())
                .toList();
    }
}
