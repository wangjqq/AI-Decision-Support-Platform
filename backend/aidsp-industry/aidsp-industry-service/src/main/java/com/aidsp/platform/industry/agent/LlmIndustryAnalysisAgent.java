package com.aidsp.platform.industry.agent;

import com.aidsp.platform.analysis.api.AnalysisAgent;
import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.api.AnalysisType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 行业分析 LLM Agent。
 * <p>基于 Spring AI ChatClient 调用真实大模型生成 6 维度 + 产业链 / 龙头 / 参考的结构化结果。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "aidsp.agent.mock-only", havingValue = "false", matchIfMissing = true)
public class LlmIndustryAnalysisAgent implements AnalysisAgent {

    private static final String SYSTEM_PROMPT = """
            你是一名资深行业研究分析师，请基于用户给出的 query 解析出目标行业，并按以下结构化字段输出。
            严格输出 JSON（不要包含 ``` 或任何额外文本），字段必须完整：
            {
              "target": "行业名",
              "overview":     {"title":"行业概况", "icon":"AppstoreOutlined",  "color":"blue",   "summary":"...", "keyPoints":["..."]},
              "marketSize":   {"title":"市场空间", "icon":"RiseOutlined",      "color":"green",  "summary":"...", "keyPoints":["..."], "metrics":{"国内市场规模":"...","复合增速(CAGR)":"...","市场集中度(CR5)":"..."}},
              "chain":        {"title":"产业链结构","icon":"BranchesOutlined", "color":"purple", "summary":"...", "keyPoints":["..."]},
              "leading":      {"title":"龙头企业", "icon":"TrophyOutlined",    "color":"gold",   "summary":"...", "keyPoints":["..."]},
              "trends":       {"title":"未来趋势", "icon":"RocketOutlined",    "color":"cyan",   "summary":"...", "keyPoints":["..."]},
              "risks":        {"title":"风险分析", "icon":"WarningOutlined",   "color":"red",    "summary":"...", "keyPoints":["..."]},
              "chainNodes":[
                {"name":"上游-原料/设备","type":"UPSTREAM",  "description":"...","representatives":"代表公司 A、B"},
                {"name":"中游-核心制造","type":"MIDSTREAM",  "description":"...","representatives":"代表公司 C、D"},
                {"name":"下游-应用与服务","type":"DOWNSTREAM","description":"...","representatives":"代表公司 E、F"}
              ],
              "leadingCompanies":[
                {"name":"龙头A","stockCode":"600000","marketShare":18.5,"tag":"全球龙头","description":"..."},
                {"name":"龙头B","stockCode":"600001","marketShare":12.3,"tag":"国内龙头","description":"..."}
              ],
              "references":[
                {"title":"...","url":"https://...","snippet":"..."}
              ]
            }
            要求：summary 2-3 句话；keyPoints 3-5 条；metrics 字段为 k-v 文本对；不编造具体股票代码 / 数字。
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmIndustryAnalysisAgent(ObjectProvider<ChatClient.Builder> builderProvider) {
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
        return AnalysisType.INDUSTRY;
    }

    @Override
    public AnalysisResultDTO run(AnalysisQueryRequest request) {
        long start = System.currentTimeMillis();
        String userPrompt = "用户问题：" + (request == null || request.getQuery() == null ? "" : request.getQuery());

        String content = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        Map<String, Object> result = parseJson(content);
        long took = System.currentTimeMillis() - start;
        log.info("[LlmIndustryAnalysisAgent] query='{}' took={}ms", userPrompt, took);

        return AnalysisResultDTO.builder()
                .analysisType(AnalysisType.INDUSTRY)
                .result(result)
                .tookMs(took)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("LLM 返回内容为空");
        }
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline > 0) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();
        }
        try {
            return objectMapper.readValue(cleaned, LinkedHashMap.class);
        } catch (JsonProcessingException e) {
            log.error("[LlmIndustryAnalysisAgent] JSON 解析失败，原始内容: {}", cleaned, e);
            throw new IllegalStateException("LLM 返回内容无法解析为 JSON: " + e.getOriginalMessage(), e);
        }
    }
}
