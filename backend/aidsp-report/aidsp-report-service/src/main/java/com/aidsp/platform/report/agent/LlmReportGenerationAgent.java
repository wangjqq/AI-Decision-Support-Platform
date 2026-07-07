package com.aidsp.platform.report.agent;

import com.aidsp.platform.analysis.api.AnalysisAgent;
import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.api.AnalysisType;
import com.aidsp.platform.company.api.CompanyAnalysisResultVO;
import com.aidsp.platform.company.api.CompanyDimensionVO;
import com.aidsp.platform.industry.api.IndustryAnalysisResultVO;
import com.aidsp.platform.report.api.ReportReferenceVO;
import com.aidsp.platform.report.api.ReportSectionVO;
import com.aidsp.platform.report.api.ReportTocItemVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报告生成 LLM Agent。
 * <p>基于 Spring AI ChatClient，接收公司 + 行业分析结果作为上下文，让 LLM 输出结构化报告 JSON。
 * <p>最终对外通过 {@link #generate(ReportAgentRequest)} 暴露给 {@code ReportServiceImpl}。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "aidsp.agent.mock-only", havingValue = "false", matchIfMissing = true)
public class LlmReportGenerationAgent implements AnalysisAgent, ReportAgent {

    private static final String SYSTEM_PROMPT = """
            你是一名资深行业研究主管，擅长把"公司分析结果"和"行业分析结果"整合成结构化 Markdown 研究报告。
            请严格输出 JSON（不要包含 ``` 或任何额外文本），字段必须完整：
            {
              "title": "...",                         // 报告标题，30 字内
              "summary": "...",                        // 200-300 字摘要，纯文本
              "summaryMarkdown": "...",                // 含 **加粗** 列表的 Markdown 摘要
              "sections": [
                {"key":"summary",  "title":"摘要",     "anchor":"section-summary",  "markdown":"..."},
                {"key":"industry", "title":"一、行业分析","anchor":"section-industry", "markdown":"..."},
                {"key":"company",  "title":"二、公司分析","anchor":"section-company",  "markdown":"..."},
                {"key":"risk",     "title":"三、风险分析","anchor":"section-risk",     "markdown":"..."},
                {"key":"conclusion","title":"四、最终结论","anchor":"section-conclusion","markdown":"..."}
              ],
              "references": [
                {"title":"...", "url":"https://...", "snippet":"..."}
              ]
            }
            要求：
            1) 每个 section 的 markdown 内部用 ### 三级标题分层，列表用 - 开头
            2) 结论章节给出可执行的决策建议（短期 / 中期 / 长期）
            3) 不要编造具体数字；可参考输入中的 keyPoints
            4) 不得出现 ``` 等代码块围栏
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmReportGenerationAgent(ObjectProvider<ChatClient.Builder> builderProvider) {
        ChatClient.Builder builder = builderProvider.getIfAvailable();
        if (builder == null) {
            throw new IllegalStateException(
                    "ChatClient.Builder 不可用：请启用至少一个 LLM Provider（OpenAI / DashScope / Ollama），"
                            + "并在 application.yml 中正确配置 api-key / base-url / model。");
        }
        this.chatClient = builder.build();
    }

    @Override
    public AnalysisType supports() {
        // 报告生成不通过 Orchestrator 调度；保留声明以便后续扩展。
        return AnalysisType.REPORT;
    }

    @Override
    public AnalysisResultDTO run(AnalysisQueryRequest request) {
        // 报告生成走 generate(ReportAgentRequest) 直接调用；此方法仅为满足接口。
        return AnalysisResultDTO.builder()
                .analysisType(AnalysisType.REPORT)
                .result(Map.of("note", "LlmReportGenerationAgent 不通过 Orchestrator 调度，请直接调用 generate()"))
                .tookMs(0L)
                .build();
    }

    /**
     * 生成完整报告。
     */
    @Override
    public ReportAgentResult generate(ReportAgentRequest req) {
        long start = System.currentTimeMillis();
        String userPrompt = buildUserPrompt(req);

        String content = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        Map<String, Object> json = parseJson(content);

        String title = strVal(json.get("title"),
                (req.title() != null && !req.title().isBlank()) ? req.title()
                        : defaultTitle(req));
        String summary = strVal(json.get("summary"), "");
        String summaryMd = strVal(json.get("summaryMarkdown"), "");

        List<ReportSectionVO> sections = toSections(json.get("sections"));
        List<ReportReferenceVO> references = toReferences(json.get("references"));
        List<ReportTocItemVO> toc = toToc(sections);

        // 拼装完整 Markdown
        String full = composeFullMarkdown(title, summaryMd, sections, references);

        long tookMs = System.currentTimeMillis() - start;
        log.info("[LlmReportGenerationAgent] generated report for company='{}' industry='{}' in {}ms",
                req.company() == null ? null : req.company().getCompanyName(),
                req.industry() == null ? null : req.industry().getIndustryName(),
                tookMs);

        return new ReportAgentResult(title, summary, summaryMd, toc, sections, full, references, tookMs);
    }

    // -------------------- Prompt --------------------

    private String buildUserPrompt(ReportAgentRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("报告主题：").append(req.title() == null || req.title().isBlank() ? defaultTitle(req) : req.title()).append("\n\n");
        sb.append("用户原始 query：").append(req.query() == null ? "" : req.query()).append("\n\n");
        sb.append("=== 公司分析结果 ===\n").append(renderCompany(req.company())).append("\n\n");
        sb.append("=== 行业分析结果 ===\n").append(renderIndustry(req.industry())).append("\n\n");
        sb.append("请基于以上素材按要求输出结构化 JSON。");
        return sb.toString();
    }

    private String renderCompany(CompanyAnalysisResultVO c) {
        if (c == null) return "（无）";
        StringBuilder sb = new StringBuilder();
        sb.append("公司名：").append(c.getCompanyName()).append("\n");
        appendDim(sb, "概览", c.getOverview());
        appendDim(sb, "主营业务", c.getMainBusiness());
        appendDim(sb, "核心优势", c.getAdvantages());
        appendDim(sb, "潜在风险", c.getRisks());
        appendDim(sb, "AI 结论", c.getAiConclusion());
        return sb.toString();
    }

    private void appendDim(StringBuilder sb, String name, CompanyDimensionVO d) {
        if (d == null) return;
        sb.append("- ").append(name).append("：").append(d.getSummary() == null ? "" : d.getSummary()).append("\n");
        if (d.getKeyPoints() != null) {
            for (String p : d.getKeyPoints()) {
                sb.append("    · ").append(p).append("\n");
            }
        }
    }

    private String renderIndustry(IndustryAnalysisResultVO i) {
        if (i == null) return "（无）";
        StringBuilder sb = new StringBuilder();
        sb.append("行业名：").append(i.getIndustryName()).append("\n");
        if (i.getOverview() != null) sb.append("概况：").append(i.getOverview().getSummary()).append("\n");
        if (i.getMarketSize() != null) sb.append("市场空间：").append(i.getMarketSize().getSummary()).append("\n");
        if (i.getChain() != null) sb.append("产业链：").append(i.getChain().getSummary()).append("\n");
        if (i.getLeading() != null) sb.append("龙头：").append(i.getLeading().getSummary()).append("\n");
        if (i.getTrends() != null) sb.append("趋势：").append(i.getTrends().getSummary()).append("\n");
        if (i.getRisks() != null) sb.append("风险：").append(i.getRisks().getSummary()).append("\n");
        return sb.toString();
    }

    private String defaultTitle(ReportAgentRequest req) {
        String cn = req.company() == null ? "目标公司" : req.company().getCompanyName();
        String in = req.industry() == null ? "相关行业" : req.industry().getIndustryName();
        return cn + " · " + in + " 研究报告";
    }

    // -------------------- JSON 解析 --------------------

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("LLM 返回内容为空");
        }
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline > 0) cleaned = cleaned.substring(firstNewline + 1);
            if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.trim();
        }
        try {
            return objectMapper.readValue(cleaned, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("[LlmReportGenerationAgent] JSON 解析失败，原始内容: {}", cleaned, e);
            throw new IllegalStateException("LLM 返回内容无法解析为 JSON: " + e.getOriginalMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<ReportSectionVO> toSections(Object raw) {
        List<ReportSectionVO> out = new ArrayList<>();
        if (!(raw instanceof List<?> list)) {
            return out;
        }
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) continue;
            String key = strVal(m.get("key"), "");
            String title = strVal(m.get("title"), "");
            String anchor = strVal(m.get("anchor"), slugify(title));
            String markdown = strVal(m.get("markdown"), "");
            out.add(ReportSectionVO.builder()
                    .key(key.isEmpty() ? null : key)
                    .title(title)
                    .anchor(anchor)
                    .markdown(markdown)
                    .build());
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<ReportReferenceVO> toReferences(Object raw) {
        List<ReportReferenceVO> out = new ArrayList<>();
        if (!(raw instanceof List<?> list)) {
            return out;
        }
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) continue;
            out.add(ReportReferenceVO.builder()
                    .title(strVal(m.get("title"), ""))
                    .url(strVal(m.get("url"), null))
                    .snippet(strVal(m.get("snippet"), null))
                    .build());
        }
        return out;
    }

    private List<ReportTocItemVO> toToc(List<ReportSectionVO> sections) {
        List<ReportTocItemVO> out = new ArrayList<>();
        for (ReportSectionVO s : sections) {
            out.add(ReportTocItemVO.builder()
                    .title(s.getTitle())
                    .anchor(s.getAnchor())
                    .level(1)
                    .build());
        }
        return out;
    }

    private String composeFullMarkdown(String title, String summaryMd, List<ReportSectionVO> sections, List<ReportReferenceVO> references) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(title).append("\n\n");
        sb.append("> 本报告由 AIDSP 报告生成 Agent 自动生成，结合公司画像与行业分析输出，")
                .append("数据时间 ").append(java.time.LocalDate.now()).append("。\n\n");
        sb.append("---\n\n");
        if (summaryMd != null && !summaryMd.isBlank()) {
            sb.append("## 摘要\n\n").append(summaryMd).append("\n\n---\n\n");
        }
        for (ReportSectionVO sec : sections) {
            if ("summary".equals(sec.getKey())) continue; // 摘要已写
            if (sec.getTitle() != null) sb.append("## ").append(sec.getTitle()).append("\n\n");
            if (sec.getMarkdown() != null && !sec.getMarkdown().isBlank()) {
                sb.append(sec.getMarkdown()).append("\n\n");
            }
            sb.append("---\n\n");
        }
        if (references != null && !references.isEmpty()) {
            sb.append("## 参考资料\n\n");
            for (ReportReferenceVO r : references) {
                sb.append("- ").append(r.getTitle() == null ? "" : r.getTitle());
                if (r.getUrl() != null && !r.getUrl().isBlank()) sb.append("（").append(r.getUrl()).append("）");
                if (r.getSnippet() != null && !r.getSnippet().isBlank()) sb.append("：").append(r.getSnippet());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String strVal(Object o, String def) {
        return o == null ? def : o.toString();
    }

    private String slugify(String s) {
        if (s == null) return "section";
        return "section-" + s.toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-");
    }
}
