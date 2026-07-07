package com.aidsp.platform.company.agent;

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
 * 公司分析 LLM Agent。
 * <p>基于 Spring AI ChatClient 调用真实大模型生成 5 维度结构化结果。
 * <p>通过 {@code aidsp.agent.mock-only=false}（默认）激活；为 true 时切换到 Mock 实现。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "aidsp.agent.mock-only", havingValue = "false", matchIfMissing = true)
public class LlmCompanyAnalysisAgent implements AnalysisAgent {

    private static final String SYSTEM_PROMPT = """
            你是一名资深公司研究分析师，请基于用户给出的 query 解析出目标公司，并按 5 维度结构化输出。
            严格输出 JSON（不要包含 ``` 或任何额外文本），字段必须完整：
            {
              "target": "公司名",
              "overview":     {"title":"公司概览", "icon":"BankOutlined",     "color":"blue",   "summary":"...", "keyPoints":["...", "..."]},
              "mainBusiness": {"title":"主营业务", "icon":"AppstoreOutlined", "color":"green",  "summary":"...", "keyPoints":["...", "..."]},
              "advantages":   {"title":"核心优势", "icon":"TrophyOutlined",   "color":"gold",   "summary":"...", "keyPoints":["...", "..."]},
              "risks":        {"title":"潜在风险", "icon":"WarningOutlined",  "color":"red",    "summary":"...", "keyPoints":["...", "..."]},
              "aiConclusion": {"title":"AI 结论",  "icon":"RobotOutlined",    "color":"purple", "summary":"...", "keyPoints":["...", "..."]}
            }
            要求：
            1) summary 2-3 句话；keyPoints 3-5 条，每条 1 句话
            2) 如不能确定具体公司，target 使用 query 原文或"目标公司"
            3) 不得编造具体数字 / 财务指标
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmCompanyAnalysisAgent(ObjectProvider<ChatClient.Builder> builderProvider) {
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
        return AnalysisType.COMPANY;
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
        log.info("[LlmCompanyAnalysisAgent] query='{}' took={}ms", userPrompt, took);

        return AnalysisResultDTO.builder()
                .analysisType(AnalysisType.COMPANY)
                .result(result)
                .tookMs(took)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("LLM 返回内容为空");
        }
        // 兼容 LLM 偶发包 ```json 围栏
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
            log.error("[LlmCompanyAnalysisAgent] JSON 解析失败，原始内容: {}", cleaned, e);
            throw new IllegalStateException("LLM 返回内容无法解析为 JSON: " + e.getOriginalMessage(), e);
        }
    }
}
