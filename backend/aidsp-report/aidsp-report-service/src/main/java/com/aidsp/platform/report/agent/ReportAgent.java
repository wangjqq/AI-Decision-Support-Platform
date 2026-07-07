package com.aidsp.platform.report.agent;

import com.aidsp.platform.company.api.CompanyAnalysisResultVO;
import com.aidsp.platform.industry.api.IndustryAnalysisResultVO;
import com.aidsp.platform.report.api.ReportReferenceVO;
import com.aidsp.platform.report.api.ReportSectionVO;
import com.aidsp.platform.report.api.ReportTocItemVO;

import java.util.List;

/**
 * 报告生成 Agent 抽象。
 * <p>由 {@link com.aidsp.platform.report.service.ReportServiceImpl} 调用，
 * <br>具体实现可以是 Mock 模板拼接（{@link ReportGenerationAgent}），
 * <br>也可以是 LLM 真实生成（{@link LlmReportGenerationAgent}），由 Spring 条件装配。
 */
public interface ReportAgent {

    /**
     * 生成完整 Markdown 报告。
     *
     * @param req 报告输入
     * @return 报告输出
     */
    ReportAgentResult generate(ReportAgentRequest req);

    /**
     * ReportAgent 输入。
     */
    record ReportAgentRequest(
            String title,
            String query,
            CompanyAnalysisResultVO company,
            IndustryAnalysisResultVO industry
    ) {
    }

    /**
     * ReportAgent 输出。
     */
    record ReportAgentResult(
            String title,
            String summary,
            String summaryMarkdown,
            List<ReportTocItemVO> toc,
            List<ReportSectionVO> sections,
            String markdown,
            List<ReportReferenceVO> references,
            long tookMs
    ) {
    }
}
