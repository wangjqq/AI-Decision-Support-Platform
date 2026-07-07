package com.aidsp.platform.company.agent;

import com.aidsp.platform.analysis.api.AnalysisAgent;
import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.api.AnalysisType;
import com.aidsp.platform.company.api.CompanyDimensionVO;
import com.aidsp.platform.company.service.CompanyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 公司分析 Agent（Mock 实现）。
 * <p>实现 {@link AnalysisAgent} 接口，
 * <code>supports() = COMPANY</code>，由 {@code OrchestratorDispatcher} 扫描注册。
 * <p>输入：用户的自然语言 query（如"分析宁德时代"），会从 query 文本中尽量匹配"宁德"/"比亚迪"等公司名作为模拟对象。
 * <p>输出：标准的 {@link AnalysisResultDTO}，<code>result</code> 字段是 5 维度结构（overview / mainBusiness / advantages / risks / aiConclusion）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyAnalysisAgent implements AnalysisAgent {

    private final CompanyMapper companyMapper;

    @Override
    public AnalysisType supports() {
        return AnalysisType.COMPANY;
    }

    @Override
    public AnalysisResultDTO run(AnalysisQueryRequest request) {
        long sleep = 1000L + ThreadLocalRandom.current().nextInt(801);
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String query = request == null || request.getQuery() == null ? "" : request.getQuery();
        String target = detectTargetCompany(query);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("target", target);
        result.put("overview", buildOverview(target));
        result.put("mainBusiness", buildMainBusiness(target));
        result.put("advantages", buildAdvantages(target));
        result.put("risks", buildRisks(target));
        result.put("aiConclusion", buildAiConclusion(target));

        log.info("[CompanyAnalysisAgent] processed query='{}' target='{}' in {}ms",
                query, target, sleep);

        return AnalysisResultDTO.builder()
                .analysisType(AnalysisType.COMPANY)
                .result(result)
                .tookMs(sleep)
                .build();
    }

    private String detectTargetCompany(String query) {
        if (query == null || query.isBlank()) {
            return "目标公司";
        }
        // 简单的关键字匹配，MVP 阶段用
        String[] keys = {"宁德时代", "宁德", "比亚迪", "隆基", "科大讯飞", "讯飞", "迈瑞"};
        for (String k : keys) {
            if (query.contains(k)) {
                return switch (k) {
                    case "宁德", "宁德时代" -> "宁德时代";
                    case "比亚迪" -> "比亚迪";
                    case "隆基" -> "隆基绿能";
                    case "科大讯飞", "讯飞" -> "科大讯飞";
                    case "迈瑞" -> "迈瑞医疗";
                    default -> k;
                };
            }
        }
        // 没匹配上就用 query 原文（截断）
        return query.length() > 12 ? query.substring(0, 12) : query;
    }

    private Map<String, Object> buildOverview(String target) {
        return Map.of(
                "title", "公司概览",
                "icon", "BankOutlined",
                "color", "blue",
                "summary", target + " 是一家在所属赛道具有领先地位的企业，业务覆盖研发、生产与销售一体化。",
                "keyPoints", List.of(
                        "在产业链中处于核心环节，具备较高的市场占有率",
                        "拥有完整的产品矩阵与稳定的客户结构",
                        "近年研发投入持续提升，技术壁垒逐步建立"
                )
        );
    }

    private Map<String, Object> buildMainBusiness(String target) {
        return Map.of(
                "title", "主营业务",
                "icon", "AppstoreOutlined",
                "color", "green",
                "summary", "主营业务围绕核心产品展开，配套服务形成稳定收入来源。",
                "keyPoints", List.of(
                        "主营产品在行业内具备差异化竞争力",
                        "海外业务占比稳步提升，区域结构持续优化",
                        "新业务线（高端 / 高毛利产品）成为重要增长极"
                )
        );
    }

    private Map<String, Object> buildAdvantages(String target) {
        return Map.of(
                "title", "核心优势",
                "icon", "TrophyOutlined",
                "color", "gold",
                "summary", "公司核心优势集中在技术、规模与生态三方面。",
                "keyPoints", List.of(
                        "技术：自研核心算法 / 工艺，关键指标行业领先",
                        "规模：产能 / 渠道 / 客户体量均处于头部",
                        "生态：上下游协同布局完善，议价能力突出"
                )
        );
    }

    private Map<String, Object> buildRisks(String target) {
        return Map.of(
                "title", "潜在风险",
                "icon", "WarningOutlined",
                "color", "red",
                "summary", "需重点关注宏观周期、原材料与竞争格局变化。",
                "keyPoints", List.of(
                        "上游原材料价格波动可能压制毛利",
                        "海外贸易 / 政策不确定性影响出口节奏",
                        "技术迭代速度加快，存在被替代风险"
                )
        );
    }

    private Map<String, Object> buildAiConclusion(String target) {
        return Map.of(
                "title", "AI 结论",
                "icon", "RobotOutlined",
                "color", "purple",
                "summary", "整体判断：基本面稳健，具备中长期投资价值。",
                "keyPoints", List.of(
                        "短期关注季报披露与新业务进展",
                        "中期受益于行业景气度提升，业绩弹性可期",
                        "长期看好技术与规模双重壁垒下的持续成长"
                )
        );
    }
}
