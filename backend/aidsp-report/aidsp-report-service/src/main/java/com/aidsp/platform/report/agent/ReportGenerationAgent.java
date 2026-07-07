package com.aidsp.platform.report.agent;

import com.aidsp.platform.analysis.api.AnalysisAgent;
import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.api.AnalysisType;
import com.aidsp.platform.company.api.CompanyAnalysisResultVO;
import com.aidsp.platform.company.api.CompanyDimensionVO;
import com.aidsp.platform.industry.api.IndustryAnalysisResultVO;
import com.aidsp.platform.industry.api.IndustryDimensionVO;
import com.aidsp.platform.report.api.ReportReferenceVO;
import com.aidsp.platform.report.api.ReportSectionVO;
import com.aidsp.platform.report.api.ReportTocItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 报告生成 Agent（Mock 实现）。
 * <p>实现 {@link AnalysisAgent} 接口，{@code supports() = REPORT}，
 * <br>由 {@code OrchestratorDispatcher} 扫描注册。
 * <p>同时实现 {@link ReportAgent} 接口，供 {@code ReportServiceImpl} 直接调用生成报告。
 * <p>当前阶段用模板拼接实现，不依赖外部 LLM；通过 {@code aidsp.agent.mock-only=true} 激活。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "aidsp.agent.mock-only", havingValue = "true")
public class ReportGenerationAgent implements AnalysisAgent, ReportAgent {

    @Override
    public AnalysisType supports() {
        return AnalysisType.REPORT;
    }

    @Override
    public AnalysisResultDTO run(AnalysisQueryRequest request) {
        // 报告生成不通过 Orchestrator 调度；保留空实现以满足 AnalysisAgent 接口。
        return AnalysisResultDTO.builder()
                .analysisType(AnalysisType.REPORT)
                .result(Map.of("note", "ReportGenerationAgent 不通过 Orchestrator 调度，请直接调用 generate()"))
                .tookMs(0L)
                .build();
    }

    /**
     * 生成完整 Markdown 报告。
     *
     * @param req 报告输入（公司 + 行业分析结果 + 用户 query）
     * @return 报告输出（Markdown + 目录 + 章节 + 参考）
     */
    @Override
    public ReportAgentResult generate(ReportAgentRequest req) {
        long start = System.currentTimeMillis();
        // 模拟思考耗时 1500-2500ms
        long sleep = 1500L + ThreadLocalRandom.current().nextInt(1001);
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        CompanyAnalysisResultVO company = req.company();
        IndustryAnalysisResultVO industry = req.industry();

        String companyName = company != null && company.getCompanyName() != null
                ? company.getCompanyName() : "目标公司";
        String industryName = industry != null && industry.getIndustryName() != null
                ? industry.getIndustryName() : "相关行业";

        // ---- 1. 摘要 ----
        String summary = buildSummary(company, industry, companyName, industryName);
        String summaryMd = buildSummaryMarkdown(company, industry, companyName, industryName);

        // ---- 2. 章节 Markdown ----
        String industrySection = buildIndustrySection(industry, industryName);
        String companySection = buildCompanySection(company, companyName);
        String riskSection = buildRiskSection(company, industry, companyName, industryName);
        String conclusionSection = buildConclusionSection(company, industry, companyName, industryName);

        // ---- 3. 章节对象 ----
        List<ReportSectionVO> sections = List.of(
                ReportSectionVO.builder()
                        .key("summary").title("摘要").anchor("section-summary")
                        .markdown(summaryMd).build(),
                ReportSectionVO.builder()
                        .key("industry").title("一、行业分析").anchor("section-industry")
                        .markdown(industrySection).build(),
                ReportSectionVO.builder()
                        .key("company").title("二、公司分析").anchor("section-company")
                        .markdown(companySection).build(),
                ReportSectionVO.builder()
                        .key("risk").title("三、风险分析").anchor("section-risk")
                        .markdown(riskSection).build(),
                ReportSectionVO.builder()
                        .key("conclusion").title("四、最终结论").anchor("section-conclusion")
                        .markdown(conclusionSection).build()
        );

        // ---- 4. 目录 ----
        List<ReportTocItemVO> toc = List.of(
                ReportTocItemVO.builder().anchor("section-summary").title("摘要").level(1).build(),
                ReportTocItemVO.builder().anchor("section-industry").title("一、行业分析").level(1).build(),
                ReportTocItemVO.builder().anchor("section-company").title("二、公司分析").level(1).build(),
                ReportTocItemVO.builder().anchor("section-risk").title("三、风险分析").level(1).build(),
                ReportTocItemVO.builder().anchor("section-conclusion").title("四、最终结论").level(1).build()
        );

        // ---- 5. 参考 ----
        List<ReportReferenceVO> references = buildReferences(company, industry, companyName, industryName);

        // ---- 6. 完整 Markdown ----
        StringBuilder full = new StringBuilder();
        full.append("# ").append(req.title() != null && !req.title().isBlank()
                ? req.title()
                : (companyName + " · " + industryName + " 研究报告")).append("\n\n");
        full.append("> 本报告由 AIDSP 报告生成 Agent 自动生成，结合公司画像与行业分析输出，")
                .append("数据时间 ").append(java.time.LocalDate.now()).append("。\n\n");
        full.append("---\n\n");
        full.append("## 摘要\n\n").append(summaryMd).append("\n\n---\n\n");
        full.append("## 一、行业分析\n\n").append(industrySection).append("\n\n---\n\n");
        full.append("## 二、公司分析\n\n").append(companySection).append("\n\n---\n\n");
        full.append("## 三、风险分析\n\n").append(riskSection).append("\n\n---\n\n");
        full.append("## 四、最终结论\n\n").append(conclusionSection).append("\n\n---\n\n");
        if (!references.isEmpty()) {
            full.append("## 参考资料\n\n");
            for (ReportReferenceVO ref : references) {
                full.append("- ").append(ref.getTitle());
                if (ref.getUrl() != null && !ref.getUrl().isBlank()) {
                    full.append("（").append(ref.getUrl()).append("）");
                }
                if (ref.getSnippet() != null && !ref.getSnippet().isBlank()) {
                    full.append("：").append(ref.getSnippet());
                }
                full.append("\n");
            }
            full.append("\n");
        }

        long tookMs = System.currentTimeMillis() - start;
        log.info("[ReportGenerationAgent] generated report for company='{}' industry='{}' in {}ms",
                companyName, industryName, tookMs);

        return new ReportAgentResult(
                req.title() != null && !req.title().isBlank()
                        ? req.title()
                        : (companyName + " · " + industryName + " 研究报告"),
                summary,
                summaryMd,
                toc,
                sections,
                full.toString(),
                references,
                tookMs
        );
    }

    // ===================== 章节构建 =====================

    private String buildSummary(CompanyAnalysisResultVO c, IndustryAnalysisResultVO i,
                                String companyName, String industryName) {
        StringBuilder sb = new StringBuilder();
        sb.append("本报告围绕 ").append(companyName);
        if (i != null) {
            sb.append(" 及其所处的 ").append(industryName).append(" 行业");
        }
        sb.append(" 展开研究。报告整合公司画像与行业分析结果，");
        sb.append("从行业景气度、公司基本面、潜在风险以及投资决策四个维度，");
        sb.append("输出一份结构化的研究判断，可作为后续投资 / 合作 / 立项决策的参考。");
        return sb.toString();
    }

    private String buildSummaryMarkdown(CompanyAnalysisResultVO c, IndustryAnalysisResultVO i,
                                        String companyName, String industryName) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildSummary(c, i, companyName, industryName)).append("\n\n");
        sb.append("**核心结论：**\n\n");
        sb.append("- **行业景气度：** ").append(industryOverview(i, industryName)).append("\n");
        sb.append("- **公司基本面：** ").append(companyOverview(c, companyName)).append("\n");
        sb.append("- **关键风险点：** ").append(riskOverview(c, i, companyName, industryName)).append("\n");
        sb.append("- **AI 决策建议：** ").append(conclusionOverview(c, i, companyName, industryName)).append("\n");
        return sb.toString();
    }

    private String buildIndustrySection(IndustryAnalysisResultVO i, String industryName) {
        StringBuilder sb = new StringBuilder();
        if (i == null) {
            sb.append("> 本次报告未关联行业分析结果，章节内容由 Agent 基于公司画像与公开行业认知生成。\n\n");
            sb.append(renderDimension(IndustryDimensionVO.builder()
                    .title("行业概况")
                    .icon("AppstoreOutlined").color("blue")
                    .summary(industryName + " 是当前宏观经济与产业升级中的关键赛道之一。")
                    .keyPoints(List.of(
                            "所属国民经济大类：制造业 - 综合",
                            "产业链定位：中游核心制造",
                            "近 3 年行业景气度处于扩张期",
                            "政策导向：受国家产业升级与创新驱动支持"
                    ))
                    .build()));
            return sb.toString();
        }
        sb.append(renderDimension(i.getOverview()));
        sb.append(renderDimension(i.getMarketSize()));
        sb.append(renderDimension(i.getChain()));
        if (i.getChainNodes() != null && !i.getChainNodes().isEmpty()) {
            sb.append("**产业链节点明细：**\n\n");
            sb.append("| 节点 | 类型 | 代表企业 | 说明 |\n");
            sb.append("| --- | --- | --- | --- |\n");
            for (var n : i.getChainNodes()) {
                sb.append("| ").append(safe(n.getName())).append(" | ")
                        .append(safe(n.getType())).append(" | ")
                        .append(safe(n.getRepresentatives())).append(" | ")
                        .append(safe(n.getDescription())).append(" |\n");
            }
            sb.append("\n");
        }
        sb.append(renderDimension(i.getLeading()));
        if (i.getLeadingCompanies() != null && !i.getLeadingCompanies().isEmpty()) {
            sb.append("**龙头企业一览：**\n\n");
            sb.append("| 名称 | 股票代码 | 市场份额 | 标签 | 描述 |\n");
            sb.append("| --- | --- | --- | --- | --- |\n");
            for (var c : i.getLeadingCompanies()) {
                sb.append("| ").append(safe(c.getName())).append(" | ")
                        .append(safe(c.getStockCode())).append(" | ")
                        .append(c.getMarketShare() == null ? "-" : (String.format("%.1f%%", c.getMarketShare())))
                        .append(" | ")
                        .append(safe(c.getTag())).append(" | ")
                        .append(safe(c.getDescription())).append(" |\n");
            }
            sb.append("\n");
        }
        sb.append(renderDimension(i.getTrends()));
        return sb.toString();
    }

    private String buildCompanySection(CompanyAnalysisResultVO c, String companyName) {
        StringBuilder sb = new StringBuilder();
        if (c == null) {
            sb.append("> 未获取到公司分析结果，章节内容由 Agent 基于公司公开信息生成。\n\n");
            return sb.toString();
        }
        sb.append(renderCompanyDimension(c.getOverview()));
        sb.append(renderCompanyDimension(c.getMainBusiness()));
        sb.append(renderCompanyDimension(c.getAdvantages()));
        sb.append(renderCompanyDimension(c.getAiConclusion()));
        return sb.toString();
    }

    private String buildRiskSection(CompanyAnalysisResultVO c, IndustryAnalysisResultVO i,
                                    String companyName, String industryName) {
        StringBuilder sb = new StringBuilder();
        sb.append("本节整合公司与行业分析中的风险维度，提炼出 ").append(companyName)
                .append(" 在当前 ").append(industryName).append(" 行业环境下的关键风险点。\n\n");

        if (c != null && c.getRisks() != null) {
            sb.append("### 公司层面风险\n\n");
            sb.append(renderCompanyDimension(c.getRisks()));
        } else {
            sb.append("### 公司层面风险\n\n");
            sb.append("- 上游原材料价格波动可能压制毛利\n");
            sb.append("- 核心客户集中度较高，存在大客户依赖\n");
            sb.append("- 海外业务汇率与贸易政策不确定性\n\n");
        }

        if (i != null && i.getRisks() != null) {
            sb.append("### 行业层面风险\n\n");
            sb.append(renderDimension(i.getRisks()));
        } else {
            sb.append("### 行业层面风险\n\n");
            sb.append("- 行业周期下行可能传导至公司收入端\n");
            sb.append("- 政策与监管变化影响行业格局\n");
            sb.append("- 新一代技术对现有产能存在替代风险\n\n");
        }
        return sb.toString();
    }

    private String buildConclusionSection(CompanyAnalysisResultVO c, IndustryAnalysisResultVO i,
                                          String companyName, String industryName) {
        StringBuilder sb = new StringBuilder();
        sb.append("综合 ").append(industryName).append(" 行业景气度与 ").append(companyName)
                .append(" 公司基本面，得出如下结论：\n\n");
        sb.append("**1. 行业判断：**  ").append(industryConclusion(i, industryName)).append("\n\n");
        sb.append("**2. 公司判断：**  ").append(companyConclusion(c, companyName)).append("\n\n");
        sb.append("**3. 投资 / 合作建议：**\n\n");
        sb.append("- **短期（0-6 个月）：** 关注季报披露、新业务进展、原材料价格走势；\n");
        sb.append("- **中期（6-18 个月）：** 跟踪行业景气度拐点，验证公司业绩弹性；\n");
        sb.append("- **长期（18 个月以上）：** 在技术与规模双壁垒下，公司有望维持中长期成长。\n\n");
        sb.append("**4. 决策结论：**  ");
        if (c != null && c.getAiConclusion() != null && c.getAiConclusion().getSummary() != null) {
            sb.append(c.getAiConclusion().getSummary());
        } else {
            sb.append(companyName).append(" 基本面稳健，建议在控制风险敞口的前提下保持关注。");
        }
        sb.append("\n");
        return sb.toString();
    }

    // ===================== 渲染辅助 =====================

    private String renderDimension(IndustryDimensionVO d) {
        if (d == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("### ").append(safe(d.getTitle())).append("\n\n");
        if (d.getSummary() != null && !d.getSummary().isBlank()) {
            sb.append(d.getSummary()).append("\n\n");
        }
        if (d.getKeyPoints() != null && !d.getKeyPoints().isEmpty()) {
            for (String kp : d.getKeyPoints()) {
                sb.append("- ").append(kp).append("\n");
            }
            sb.append("\n");
        }
        if (d.getMetrics() != null && !d.getMetrics().isEmpty()) {
            sb.append("**关键指标：**\n\n");
            sb.append("| 指标 | 数值 |\n| --- | --- |\n");
            d.getMetrics().forEach((k, v) -> sb.append("| ").append(safe(k)).append(" | ")
                    .append(safe(v)).append(" |\n"));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String renderCompanyDimension(CompanyDimensionVO d) {
        if (d == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("### ").append(safe(d.getTitle())).append("\n\n");
        if (d.getSummary() != null && !d.getSummary().isBlank()) {
            sb.append(d.getSummary()).append("\n\n");
        }
        if (d.getKeyPoints() != null && !d.getKeyPoints().isEmpty()) {
            for (String kp : d.getKeyPoints()) {
                sb.append("- ").append(kp).append("\n");
            }
            sb.append("\n");
        }
        if (d.getMetrics() != null && !d.getMetrics().isEmpty()) {
            sb.append("**关键指标：**\n\n");
            sb.append("| 指标 | 数值 |\n| --- | --- |\n");
            d.getMetrics().forEach((k, v) -> sb.append("| ").append(safe(k)).append(" | ")
                    .append(safe(v)).append(" |\n"));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("|", "\\|");
    }

    // ===================== 概述短语 =====================

    private String industryOverview(IndustryAnalysisResultVO i, String name) {
        if (i != null && i.getOverview() != null && i.getOverview().getSummary() != null) {
            return i.getOverview().getSummary();
        }
        return name + " 处于扩张期，行业景气度持续向上。";
    }

    private String companyOverview(CompanyAnalysisResultVO c, String name) {
        if (c != null && c.getOverview() != null && c.getOverview().getSummary() != null) {
            return c.getOverview().getSummary();
        }
        return name + " 在所属赛道具有领先地位，业绩弹性可期。";
    }

    private String riskOverview(CompanyAnalysisResultVO c, IndustryAnalysisResultVO i,
                                String companyName, String industryName) {
        if (c != null && c.getRisks() != null && c.getRisks().getSummary() != null) {
            return c.getRisks().getSummary();
        }
        return "需重点关注上游周期、宏观政策与海外贸易不确定性。";
    }

    private String conclusionOverview(CompanyAnalysisResultVO c, IndustryAnalysisResultVO i,
                                      String companyName, String industryName) {
        if (c != null && c.getAiConclusion() != null && c.getAiConclusion().getSummary() != null) {
            return c.getAiConclusion().getSummary();
        }
        return "基本面稳健，建议在控制风险敞口的前提下中长期关注。";
    }

    private String industryConclusion(IndustryAnalysisResultVO i, String name) {
        if (i != null && i.getTrends() != null && i.getTrends().getSummary() != null) {
            return i.getTrends().getSummary();
        }
        return name + " 未来 3-5 年技术、政策、出海三因素共振，行业仍有结构性机会。";
    }

    private String companyConclusion(CompanyAnalysisResultVO c, String name) {
        if (c != null && c.getAiConclusion() != null && c.getAiConclusion().getSummary() != null) {
            return c.getAiConclusion().getSummary();
        }
        return name + " 技术与规模双壁垒稳固，业绩弹性可期。";
    }

    // ===================== 参考资料 =====================

    private List<ReportReferenceVO> buildReferences(CompanyAnalysisResultVO c, IndustryAnalysisResultVO i,
                                                     String companyName, String industryName) {
        List<ReportReferenceVO> refs = new ArrayList<>();
        if (i != null && i.getReferences() != null) {
            for (var r : i.getReferences()) {
                refs.add(ReportReferenceVO.builder()
                        .title(r.getTitle())
                        .url(r.getUrl())
                        .snippet(r.getSnippet())
                        .sourceType("INDUSTRY")
                        .build());
            }
        }
        if (c != null && c.getReferences() != null) {
            for (var r : c.getReferences()) {
                refs.add(ReportReferenceVO.builder()
                        .title(r.getTitle())
                        .url(r.getUrl())
                        .snippet(r.getSnippet())
                        .sourceType("COMPANY")
                        .build());
            }
        }
        if (refs.isEmpty()) {
            refs.add(ReportReferenceVO.builder()
                    .title(industryName + "产业白皮书")
                    .url("https://example.com/whitepaper")
                    .snippet(industryName + " 全产业链深度研究。")
                    .sourceType("INDUSTRY").build());
            refs.add(ReportReferenceVO.builder()
                    .title(companyName + "公司公告与年报")
                    .url("https://example.com/annual")
                    .snippet(companyName + " 历年财务与业务披露。")
                    .sourceType("COMPANY").build());
        }
        return refs;
    }

    // ===================== 入参 / 出参 =====================
    // 内联 record 已被抽到 ReportAgent 接口，ReportServiceImpl 统一通过接口使用。
}
