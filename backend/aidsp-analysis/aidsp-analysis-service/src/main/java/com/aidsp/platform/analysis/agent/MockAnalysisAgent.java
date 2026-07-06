package com.aidsp.platform.analysis.agent;

import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.api.AnalysisType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 通用 Mock 分析 Agent。
 * <p>supports() 返回 null，标识兜底支持所有类型；模拟耗时 800~1500ms 后返回样例结果。
 */
@Slf4j
@Service
@Primary
public class MockAnalysisAgent implements AnalysisAgent {

    @Override
    public AnalysisType supports() {
        // null 表示兜底
        return null;
    }

    @Override
    public AnalysisResultDTO run(AnalysisQueryRequest request) {
        long sleep = 800L + ThreadLocalRandom.current().nextInt(701);
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", buildSummary(request.getQuery()));
        result.put("keyPoints", List.of(
                "关键观点 1：聚焦用户问题核心并给出结构化结论",
                "关键观点 2：综合多源信息形成趋势性判断",
                "关键观点 3：提示潜在风险与机会并存"
        ));
        result.put("metrics", List.of(
                Map.of("name", "置信度", "value", 0.82, "unit", "score"),
                Map.of("name", "覆盖度", "value", 0.76, "unit", "score")
        ));
        result.put("references", List.of(
                Map.of("title", "示例数据源 A", "url", "https://example.com/a"),
                Map.of("title", "示例数据源 B", "url", "https://example.com/b")
        ));

        log.info("[MockAnalysisAgent] processed query in {}ms", sleep);

        return AnalysisResultDTO.builder()
                .analysisType(AnalysisType.COMPANY)
                .result(result)
                .tookMs(sleep)
                .build();
    }

    private String buildSummary(String query) {
        return "针对问题「" + query + "」，已结合多维度数据进行初步研判。"
                + "结论显示该问题在当前数据覆盖下具备可分析价值，建议结合行业与企业基本面进一步验证。"
                + "后续将通过 RAG 检索与领域 Agent 提供更深入的洞察。";
    }
}
