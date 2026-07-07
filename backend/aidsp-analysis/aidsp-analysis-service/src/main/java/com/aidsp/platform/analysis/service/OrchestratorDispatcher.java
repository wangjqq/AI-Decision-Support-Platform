package com.aidsp.platform.analysis.service;

import com.aidsp.platform.analysis.api.AnalysisAgent;
import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.api.AnalysisType;
import com.aidsp.platform.core.exception.BusinessException;
import com.aidsp.platform.core.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 分析调度器。
 * <p>根据 AnalysisType 选择 Agent，通过线程池异步执行并设置 60s 超时。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorDispatcher {

    private static final long TIMEOUT_SECONDS = 60L;

    private final List<AnalysisAgent> agents;

    private final Map<AnalysisType, AnalysisAgent> typedAgents = new EnumMap<>(AnalysisType.class);

    private AnalysisAgent fallbackAgent;

    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "analysis-agent-worker");
        t.setDaemon(true);
        return t;
    });

    @PostConstruct
    public void init() {
        for (AnalysisAgent agent : agents) {
            AnalysisType supports = agent.supports();
            if (supports == null) {
                fallbackAgent = agent;
                log.info("[OrchestratorDispatcher] register fallback agent: {}",
                        agent.getClass().getSimpleName());
            } else {
                typedAgents.put(supports, agent);
                log.info("[OrchestratorDispatcher] register typed agent: {} -> {}",
                        agent.getClass().getSimpleName(), supports);
            }
        }
    }

    /**
     * 调度并执行分析。
     */
    public AnalysisResultDTO dispatch(AnalysisType type, AnalysisQueryRequest request) {
        AnalysisAgent agent = typedAgents.get(type);
        if (agent == null) {
            agent = fallbackAgent;
        }
        if (agent == null) {
            throw new BusinessException(ErrorCode.AGENT_RUN_FAILED, "无可用的分析 Agent");
        }
        // 复制到 final 引用，避免 lambda 捕获非 effectively final 变量
        final AnalysisAgent selectedAgent = agent;
        log.info("[OrchestratorDispatcher] dispatch type={}, agent={}", type,
                selectedAgent.getClass().getSimpleName());

        Future<AnalysisResultDTO> future = executor.submit(() -> selectedAgent.run(request));
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.error("[OrchestratorDispatcher] agent timeout after {}s", TIMEOUT_SECONDS);
            throw new BusinessException(ErrorCode.ANALYSIS_TIMEOUT);
        } catch (Exception e) {
            future.cancel(true);
            log.error("[OrchestratorDispatcher] agent run failed", e);
            throw new BusinessException(ErrorCode.AGENT_RUN_FAILED, e.getMessage());
        }
    }
}
