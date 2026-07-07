package com.aidsp.platform.industry.service;

import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.util.NanoId;
import com.aidsp.platform.industry.agent.IndustryAnalysisAgent;
import com.aidsp.platform.industry.api.IndustryAnalysisHistoryItemVO;
import com.aidsp.platform.industry.api.IndustryAnalysisRequest;
import com.aidsp.platform.industry.api.IndustryAnalysisResultVO;
import com.aidsp.platform.industry.api.IndustryChainNodeVO;
import com.aidsp.platform.industry.api.IndustryDimensionVO;
import com.aidsp.platform.industry.api.IndustryLeadingCompanyVO;
import com.aidsp.platform.industry.api.IndustryAnalysisReferenceVO;
import com.aidsp.platform.industry.entity.Industry;
import com.aidsp.platform.industry.entity.IndustryAnalysisResult;
import com.aidsp.platform.industry.repository.IndustryAnalysisRepository;
import com.aidsp.platform.industry.repository.IndustryRepository;
import com.aidsp.platform.core.api.PageResponse;
import com.aidsp.platform.core.exception.BusinessException;
import com.aidsp.platform.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 行业分析服务。
 * <p>页面直驱：注入 {@link IndustryAnalysisAgent}，调用 Agent.run() 拿原始 6 维度结果，
 * <br>包装为 {@link IndustryAnalysisResultVO} 并落 {@link IndustryAnalysisRepository}。
 * <p><b>不走</b> {@code OrchestratorDispatcher}：因为行业详情页是用户明确选择的目标，
 * <br>不需要 Intent 识别；但 Agent 仍被 Orchestrator 扫描（Search 入口会用）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryAnalysisService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IndustryRepository industryRepository;
    private final IndustryAnalysisRepository analysisRepository;
    private final IndustryAnalysisAgent agent;
    private final IndustryMapper industryMapper;

    /**
     * 触发一次行业分析。
     */
    public IndustryAnalysisResultVO analyze(Long industryId, IndustryAnalysisRequest request) {
        Industry industry = industryRepository.findById(industryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INDUSTRY_NOT_FOUND));

        AnalysisQueryRequest aqr = new AnalysisQueryRequest();
        aqr.setQuery(request == null || request.getQuery() == null || request.getQuery().isBlank()
                ? ("分析 " + industry.getName())
                : request.getQuery());
        aqr.setTopK(request == null || request.getTopK() == null ? 5 : request.getTopK());
        aqr.setContext(request == null ? null : request.getContext());

        // 同步调用（Agent 内部已用 Thread.sleep 模拟 1200-2000ms）
        AnalysisResultDTO raw = agent.run(aqr);
        Map<String, Object> r = raw.getResult() == null ? Collections.emptyMap() : raw.getResult();

        IndustryAnalysisResult result = new IndustryAnalysisResult();
        result.setAnalysisId(buildAnalysisId());
        result.setIndustryId(industryId);
        result.setIndustryName(industry.getName());
        result.setTookMs(raw.getTookMs());
        result.setCreatedAt(LocalDateTime.now());
        result.setOverview(toDimension(r.get("overview")));
        result.setMarketSize(toDimension(r.get("marketSize")));
        result.setChain(toDimension(r.get("chain")));
        result.setLeading(toDimension(r.get("leading")));
        result.setTrends(toDimension(r.get("trends")));
        result.setRisks(toDimension(r.get("risks")));
        result.setChainNodes(toChainNodes(r.get("chainNodes")));
        result.setLeadingCompanies(toLeadingCompanies(r.get("leadingCompanies")));
        result.setReferences(toReferences(r.get("references")));

        analysisRepository.save(result);
        log.info("[IndustryAnalysisService] industryId={} analysisId={} tookMs={}",
                industryId, result.getAnalysisId(), result.getTookMs());
        return industryMapper.toAnalysisVO(result);
    }

    /**
     * 按 analysisId 查询单次分析结果。
     */
    public IndustryAnalysisResultVO getById(Long industryId, String analysisId) {
        industryRepository.findById(industryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INDUSTRY_NOT_FOUND));
        return analysisRepository.findById(analysisId)
                .map(industryMapper::toAnalysisVO)
                .orElseThrow(() -> new BusinessException(ErrorCode.INDUSTRY_ANALYSIS_NOT_FOUND));
    }

    /**
     * 分页查询某行业的分析历史。
     */
    public PageResponse<IndustryAnalysisHistoryItemVO> pageHistory(Long industryId, Long page, Long size) {
        industryRepository.findById(industryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INDUSTRY_NOT_FOUND));
        return analysisRepository.pageByIndustry(industryId, page, size);
    }

    /**
     * 删除某次分析记录。
     */
    public void deleteAnalysis(Long industryId, String analysisId) {
        industryRepository.findById(industryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INDUSTRY_NOT_FOUND));
        boolean removed = analysisRepository.deleteById(analysisId);
        if (!removed) {
            throw new BusinessException(ErrorCode.INDUSTRY_ANALYSIS_NOT_FOUND);
        }
    }

    // -------------------- 类型转换辅助 --------------------

    @SuppressWarnings("unchecked")
    private IndustryDimensionVO toDimension(Object raw) {
        if (!(raw instanceof Map<?, ?> m)) {
            return null;
        }
        IndustryDimensionVO.IndustryDimensionVOBuilder b = IndustryDimensionVO.builder()
                .title((String) m.get("title"))
                .icon((String) m.get("icon"))
                .color((String) m.get("color"))
                .summary((String) m.get("summary"))
                .keyPoints((List<String>) m.get("keyPoints"));
        Object metrics = m.get("metrics");
        if (metrics instanceof Map<?, ?> mm) {
            b.metrics((Map<String, String>) mm);
        }
        return b.build();
    }

    @SuppressWarnings("unchecked")
    private List<IndustryChainNodeVO> toChainNodes(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(e -> e instanceof Map<?, ?>)
                .map(e -> {
                    Map<String, Object> m = (Map<String, Object>) e;
                    return industryMapper.buildChainNode(
                            (String) m.get("name"),
                            (String) m.get("type"),
                            (String) m.get("description"),
                            (String) m.get("representatives")
                    );
                })
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<IndustryLeadingCompanyVO> toLeadingCompanies(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(e -> e instanceof Map<?, ?>)
                .map(e -> {
                    Map<String, Object> m = (Map<String, Object>) e;
                    Object share = m.get("marketShare");
                    Double shareD = null;
                    if (share instanceof Number n) {
                        shareD = n.doubleValue();
                    }
                    return industryMapper.buildLeadingCompany(
                            (String) m.get("name"),
                            (String) m.get("stockCode"),
                            shareD,
                            (String) m.get("tag"),
                            (String) m.get("description")
                    );
                })
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<IndustryAnalysisReferenceVO> toReferences(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(e -> e instanceof Map<?, ?>)
                .map(e -> {
                    Map<String, Object> m = (Map<String, Object>) e;
                    return IndustryAnalysisReferenceVO.builder()
                            .title((String) m.get("title"))
                            .url((String) m.get("url"))
                            .snippet((String) m.get("snippet"))
                            .build();
                })
                .toList();
    }

    private String buildAnalysisId() {
        return "ia-" + LocalDate.now().format(DATE_FMT) + "-" + NanoId.nanoId(12);
    }
}
