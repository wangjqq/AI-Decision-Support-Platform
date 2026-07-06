package com.aidsp.platform.company.service;

import com.aidsp.platform.company.api.CompanyVO;
import com.aidsp.platform.company.entity.Company;
import com.aidsp.platform.company.api.CompanyDimensionVO;
import com.aidsp.platform.company.api.CompanyAnalysisResultVO;
import com.aidsp.platform.company.entity.CompanyAnalysisResult;
import com.aidsp.platform.company.repository.CompanyRepository;
import com.aidsp.platform.company.repository.CompanyAnalysisRepository;
import org.springframework.stereotype.Component;

/**
 * Entity ↔ VO 手动映射工具。
 * <p>MVP 阶段不引入 MapStruct，避免额外依赖。
 */
@Component
public class CompanyMapper {

    private final CompanyRepository companyRepository;
    private final CompanyAnalysisRepository analysisRepository;

    public CompanyMapper(CompanyRepository companyRepository,
                         CompanyAnalysisRepository analysisRepository) {
        this.companyRepository = companyRepository;
        this.analysisRepository = analysisRepository;
    }

    public CompanyVO toVO(Company c) {
        if (c == null) {
            return null;
        }
        CompanyVO v = new CompanyVO();
        v.setId(c.getId());
        v.setName(c.getName());
        v.setUscc(c.getUscc());
        v.setIndustryId(c.getIndustryId());
        v.setIndustryName(c.getIndustryName());
        v.setMainBusiness(c.getMainBusiness());
        v.setAddress(c.getAddress());
        v.setEstablishedAt(c.getEstablishedAt());
        v.setDescription(c.getDescription());
        v.setCreatedAt(c.getCreatedAt());
        v.setUpdatedAt(c.getUpdatedAt());
        return v;
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
                                              String summary, java.util.List<String> keyPoints) {
        return CompanyDimensionVO.builder()
                .title(title)
                .icon(icon)
                .color(color)
                .summary(summary)
                .keyPoints(keyPoints)
                .build();
    }
}
