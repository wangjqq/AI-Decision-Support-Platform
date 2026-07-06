package com.aidsp.platform.analysis.service;

import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.api.AnalysisService;
import com.aidsp.platform.analysis.api.AnalysisType;
import com.aidsp.platform.analysis.service.cache.AnalysisCache;
import com.aidsp.platform.analysis.util.NanoId;
import com.aidsp.platform.core.exception.BusinessException;
import com.aidsp.platform.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 智能分析服务实现。
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IntentResolver intentResolver;
    private final OrchestratorDispatcher orchestrator;
    private final AnalysisCache cache;

    @Override
    public AnalysisResultDTO analyze(AnalysisQueryRequest request) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) {
            throw new BusinessException(ErrorCode.ANALYSIS_QUERY_EMPTY);
        }
        long start = System.currentTimeMillis();

        AnalysisType type = intentResolver.resolve(request.getQuery());
        if (type == null) {
            throw new BusinessException(ErrorCode.ANALYSIS_TYPE_UNRESOLVED);
        }

        AnalysisResultDTO result = orchestrator.dispatch(type, request);
        long took = System.currentTimeMillis() - start;

        String queryId = buildQueryId();
        result.setQueryId(queryId);
        result.setAnalysisType(type);
        result.setTookMs(took);
        cache.put(queryId, result);

        log.info("[AnalysisService] queryId={}, type={}, took={}ms", queryId, type, took);
        return result;
    }

    private String buildQueryId() {
        return "q-" + LocalDate.now().format(DATE_FMT) + "-" + NanoId.nanoId(12);
    }
}
