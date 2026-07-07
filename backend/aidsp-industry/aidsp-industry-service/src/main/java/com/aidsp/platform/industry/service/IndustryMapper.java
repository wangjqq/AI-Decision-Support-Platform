package com.aidsp.platform.industry.service;

import com.aidsp.platform.industry.api.IndustryAnalysisResultVO;
import com.aidsp.platform.industry.api.IndustryChainNodeVO;
import com.aidsp.platform.industry.api.IndustryDimensionVO;
import com.aidsp.platform.industry.api.IndustryLeadingCompanyVO;
import com.aidsp.platform.industry.api.IndustryVO;
import com.aidsp.platform.industry.entity.Industry;
import com.aidsp.platform.industry.entity.IndustryAnalysisResult;
import com.aidsp.platform.industry.repository.IndustryRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Entity ↔ VO 手动映射工具。
 * <p>MVP 阶段不引入 MapStruct，避免额外依赖。
 */
@Component
public class IndustryMapper {

    private final IndustryRepository industryRepository;

    public IndustryMapper(IndustryRepository industryRepository) {
        this.industryRepository = industryRepository;
    }

    public IndustryVO toVO(Industry i) {
        if (i == null) {
            return null;
        }
        IndustryVO v = new IndustryVO();
        v.setId(i.getId());
        v.setCode(i.getCode());
        v.setName(i.getName());
        v.setLevel(i.getLevel());
        v.setParentId(i.getParentId());
        v.setParentName(resolveParentName(i));
        v.setDescription(i.getDescription());
        v.setTags(i.getTags());
        v.setStatus(i.getStatus());
        v.setCreatedAt(i.getCreatedAt());
        v.setUpdatedAt(i.getUpdatedAt());
        return v;
    }

    private String resolveParentName(Industry i) {
        if (i.getParentId() == null) {
            return null;
        }
        return industryRepository.findById(i.getParentId())
                .map(Industry::getName)
                .orElse(null);
    }

    public IndustryAnalysisResultVO toAnalysisVO(IndustryAnalysisResult r) {
        if (r == null) {
            return null;
        }
        IndustryAnalysisResultVO v = new IndustryAnalysisResultVO();
        v.setAnalysisId(r.getAnalysisId());
        v.setIndustryId(r.getIndustryId());
        v.setIndustryName(r.getIndustryName());
        v.setTookMs(r.getTookMs());
        v.setCreatedAt(r.getCreatedAt());
        v.setOverview(r.getOverview());
        v.setMarketSize(r.getMarketSize());
        v.setChain(r.getChain());
        v.setLeading(r.getLeading());
        v.setTrends(r.getTrends());
        v.setRisks(r.getRisks());
        v.setChainNodes(r.getChainNodes());
        v.setLeadingCompanies(r.getLeadingCompanies());
        v.setReferences(r.getReferences());
        return v;
    }

    /**
     * 供 IndustryAnalysisService 构造维度 VO 使用。
     */
    public IndustryDimensionVO buildDimension(String title, String icon, String color,
                                              String summary, List<String> keyPoints) {
        return IndustryDimensionVO.builder()
                .title(title)
                .icon(icon)
                .color(color)
                .summary(summary)
                .keyPoints(keyPoints)
                .build();
    }

    public IndustryChainNodeVO buildChainNode(String name, String type,
                                              String description, String representatives) {
        return IndustryChainNodeVO.builder()
                .name(name)
                .type(type)
                .description(description)
                .representatives(representatives)
                .build();
    }

    public IndustryLeadingCompanyVO buildLeadingCompany(String name, String stockCode,
                                                         Double marketShare, String tag,
                                                         String description) {
        return IndustryLeadingCompanyVO.builder()
                .name(name)
                .stockCode(stockCode)
                .marketShare(marketShare)
                .tag(tag)
                .description(description)
                .build();
    }
}
