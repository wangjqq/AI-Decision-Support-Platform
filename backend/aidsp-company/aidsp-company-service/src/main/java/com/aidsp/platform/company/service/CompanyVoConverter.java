package com.aidsp.platform.company.service;

import com.aidsp.platform.company.api.CompanyAnalysisResultVO;
import com.aidsp.platform.company.api.CompanyDimensionVO;
import com.aidsp.platform.company.api.CompanyFinancialVO;
import com.aidsp.platform.company.api.CompanyVO;
import com.aidsp.platform.company.entity.Company;
import com.aidsp.platform.company.entity.CompanyAnalysisResult;
import com.aidsp.platform.company.repository.CompanyAnalysisRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Entity ↔ VO 手动映射器（DTO Converter）。
 * <p>避免与 {@code repository.CompanyMapper}（MyBatis-Plus 接口）同名冲突，故命名为 {@code CompanyVoConverter}。
 * <p>DB 中 {@code revenue / profit / financial_period} 是扁平列，组装为 {@link CompanyFinancialVO} 返回。
 */
@Component
public class CompanyVoConverter {

    @SuppressWarnings("unused")
    private final CompanyAnalysisRepository analysisRepository;

    public CompanyVoConverter(CompanyAnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    public CompanyVO toVO(Company c) {
        if (c == null) {
            return null;
        }
        CompanyVO v = new CompanyVO();
        v.setId(c.getId());
        v.setName(c.getName());
        v.setCode(c.getCode());
        v.setUscc(c.getUscc());
        v.setIndustryId(c.getIndustryId());
        v.setIndustryName(c.getIndustryName());
        v.setIndustry(c.getIndustry());
        v.setMainBusiness(c.getMainBusiness());
        v.setBusiness(c.getBusiness());
        v.setAddress(c.getAddress());
        v.setEstablishedAt(c.getEstablishedAt());
        v.setDescription(c.getDescription());
        v.setFinancial(toFinancialVO(c));
        v.setCreatedAt(c.getCreatedAt());
        v.setUpdatedAt(c.getUpdatedAt());
        return v;
    }

    /** 从扁平字段构造嵌套财务 VO。 */
    private CompanyFinancialVO toFinancialVO(Company c) {
        if (c.getRevenue() == null && c.getProfit() == null && c.getFinancialPeriod() == null) {
            return null;
        }
        return CompanyFinancialVO.builder()
                .revenue(c.getRevenue())
                .profit(c.getProfit())
                .period(c.getFinancialPeriod())
                .build();
    }

    public CompanyAnalysisResultVO toAnalysisVO(CompanyAnalysisResult r) {
        if (r == null) {
            return null;
        }
        CompanyAnalysisResultVO v = new CompanyAnalysisResultVO();
        v.setAnalysisId(r.getAnalysisId());
        v.setCompanyId(r.getCompanyId());
        v.setCompanyName(r.getCompanyName());
        v.setTookMs(r.getTookMs());
        v.setCreatedAt(r.getCreatedAt());
        v.setOverview(r.getOverview());
        v.setMainBusiness(r.getMainBusiness());
        v.setAdvantages(r.getAdvantages());
        v.setRisks(r.getRisks());
        v.setAiConclusion(r.getAiConclusion());
        v.setReferences(r.getReferences());
        return v;
    }

    /**
     * 供 CompanyAnalysisService 构造维度 VO 使用。
     */
    public CompanyDimensionVO buildDimension(String title, String icon, String color,
                                              String summary, List<String> keyPoints) {
        return CompanyDimensionVO.builder()
                .title(title)
                .icon(icon)
                .color(color)
                .summary(summary)
                .keyPoints(keyPoints)
                .build();
    }
}
