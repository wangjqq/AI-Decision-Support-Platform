package com.aidsp.platform.company.service;

import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.api.AnalysisType;
import com.aidsp.platform.analysis.util.NanoId;
import com.aidsp.platform.company.agent.CompanyAnalysisAgent;
import com.aidsp.platform.company.api.CompanyAnalysisHistoryItemVO;
import com.aidsp.platform.company.api.CompanyAnalysisRequest;
import com.aidsp.platform.company.api.CompanyAnalysisResultVO;
import com.aidsp.platform.company.api.CompanyDimensionVO;
import com.aidsp.platform.company.entity.Company;
import com.aidsp.platform.company.entity.CompanyAnalysisResult;
import com.aidsp.platform.company.repository.CompanyAnalysisRepository;
import com.aidsp.platform.company.repository.CompanyRepository;
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
import java.util.concurrent.TimeUnit;

/**
 * 公司分析服务。
 * <p>页面直驱：注入 {@link CompanyAnalysisAgent}，调用 Agent.run() 拿原始 5 维度结果，
 * <br>包装为 {@link CompanyAnalysisResultVO} 并落 {@link CompanyAnalysisRepository}。
 * <p><b>不走</b> {@code OrchestratorDispatcher}：因为公司详情页是用户明确选择的目标，
 * <br>不需要 Intent 识别；但 Agent 仍被 Orchestrator 扫描（Search 入口会用）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyAnalysisService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CompanyRepository companyRepository;
    private final CompanyAnalysisRepository analysisRepository;
    private final CompanyAnalysisAgent agent;
    private final CompanyMapper companyMapper;

    /**
     * 触发一次公司分析。
     */
    public CompanyAnalysisResultVO analyze(Long companyId, CompanyAnalysisRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        AnalysisQueryRequest aqr = new AnalysisQueryRequest();
        aqr.setQuery(request == null || request.getQuery() == null || request.getQuery().isBlank()
                ? ("分析 " + company.getName())
                : request.getQuery());
        aqr.setTopK(request == null || request.getTopK() == null ? 5 : request.getTopK());
        aqr.setContext(request == null ? null : request.getContext());

        // 同步调用（Agent 内部已用 Thread.sleep 模拟 1000-1800ms）
        AnalysisResultDTO raw = agent.run(aqr);
        Map<String, Object> r = raw.getResult() == null ? Collections.emptyMap() : raw.getResult();

        CompanyAnalysisResult result = new CompanyAnalysisResult();
        result.setAnalysisId(buildAnalysisId());
        result.setCompanyId(companyId);
        result.setCompanyName(company.getName());
        result.setTookMs(raw.getTookMs());
        result.setCreatedAt(LocalDateTime.now());
        result.setOverview(toDimension(r.get("overview")));
        result.setMainBusiness(toDimension(r.get("mainBusiness")));
        result.setAdvantages(toDimension(r.get("advantages")));
        result.setRisks(toDimension(r.get("risks")));
        result.setAiConclusion(toDimension(r.get("aiConclusion")));

        analysisRepository.save(result);
        log.info("[CompanyAnalysisService] companyId={} analysisId={} tookMs={}",
                companyId, result.getAnalysisId(), result.getTookMs());
        return companyMapper.toAnalysisVO(result);
    }

    /**
     * 按 analysisId 查询单次分析结果。
     */
    public CompanyAnalysisResultVO getById(Long companyId, String analysisId) {
        // 校验公司存在
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
        return analysisRepository.findById(analysisId)
                .map(companyMapper::toAnalysisVO)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_ANALYSIS_NOT_FOUND));
    }

    /**
     * 分页查询某公司的分析历史。
     */
    public PageResponse<CompanyAnalysisHistoryItemVO> pageHistory(Long companyId, Long page, Long size) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
        return analysisRepository.pageByCompany(companyId, page, size);
    }

    /**
     * 删除某次分析记录。
     */
    public void deleteAnalysis(Long companyId, String analysisId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
        boolean removed = analysisRepository.deleteById(analysisId);
        if (!removed) {
            throw new BusinessException(ErrorCode.COMPANY_ANALYSIS_NOT_FOUND);
        }
    }

    /**
     * 将 Agent 返回的 Map（包含 title/icon/color/summary/keyPoints）转换为 {@link CompanyDimensionVO}。
     */
    @SuppressWarnings("unchecked")
    private CompanyDimensionVO toDimension(Object raw) {
        if (!(raw instanceof Map<?, ?> m)) {
            return null;
        }
        return CompanyDimensionVO.builder()
                .title((String) m.get("title"))
                .icon((String) m.get("icon"))
                .color((String) m.get("color"))
                .summary((String) m.get("summary"))
                .keyPoints((List<String>) m.get("keyPoints"))
                .build();
    }

    private String buildAnalysisId() {
        return "ca-" + LocalDate.now().format(DATE_FMT) + "-" + NanoId.nanoId(12);
    }
}
