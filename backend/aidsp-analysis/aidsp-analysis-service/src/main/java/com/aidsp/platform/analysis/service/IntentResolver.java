package com.aidsp.platform.analysis.service;

import com.aidsp.platform.analysis.api.AnalysisType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 关键词字典法意图识别器。
 * <p>命中计数取最大；并列时按 COMPANY > INDUSTRY > REPORT 优先级。
 */
@Component
public class IntentResolver {

    private static final List<String> COMPANY_KW = List.of(
            "公司", "企业", "集团", "上市", "财报", "宁德", "比亚迪"
    );

    private static final List<String> INDUSTRY_KW = List.of(
            "行业", "产业", "赛道", "前景", "市场规模", "光伏", "新能源"
    );

    private static final List<String> REPORT_KW = List.of(
            "报告", "研报", "白皮书", "年报", "季报", "分析报告"
    );

    /** 优先级：值越大越优先。 */
    private static final Map<AnalysisType, Integer> PRIORITY = Map.of(
            AnalysisType.COMPANY, 3,
            AnalysisType.INDUSTRY, 2,
            AnalysisType.REPORT, 1
    );

    /**
     * 识别用户问题意图。
     *
     * @param query 用户原始问题
     * @return 命中的分析类型；无法识别返回 null
     */
    public AnalysisType resolve(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        int c = count(query, COMPANY_KW);
        int i = count(query, INDUSTRY_KW);
        int r = count(query, REPORT_KW);

        int max = Math.max(c, Math.max(i, r));
        if (max == 0) {
            return null;
        }

        // 并列时按 PRIORITY 取最大
        AnalysisType winner = null;
        int winnerPriority = -1;
        if (c == max && PRIORITY.get(AnalysisType.COMPANY) > winnerPriority) {
            winner = AnalysisType.COMPANY;
            winnerPriority = PRIORITY.get(AnalysisType.COMPANY);
        }
        if (i == max && PRIORITY.get(AnalysisType.INDUSTRY) > winnerPriority) {
            winner = AnalysisType.INDUSTRY;
            winnerPriority = PRIORITY.get(AnalysisType.INDUSTRY);
        }
        if (r == max && PRIORITY.get(AnalysisType.REPORT) > winnerPriority) {
            winner = AnalysisType.REPORT;
            winnerPriority = PRIORITY.get(AnalysisType.REPORT);
        }
        return winner;
    }

    private int count(String text, List<String> keywords) {
        int n = 0;
        for (String kw : keywords) {
            int idx = 0;
            while ((idx = text.indexOf(kw, idx)) != -1) {
                n++;
                idx += kw.length();
            }
        }
        return n;
    }
}
