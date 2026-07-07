package com.aidsp.platform.industry.agent;

import com.aidsp.platform.analysis.api.AnalysisAgent;
import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.api.AnalysisType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 行业分析 Agent（Mock 实现）。
 * <p>实现 {@link AnalysisAgent} 接口，
 * <code>supports() = INDUSTRY</code>，由 {@code OrchestratorDispatcher} 扫描注册。
 * <p>输入：用户的自然语言 query（如"分析光伏行业"），会从 query 文本中尽量匹配"光伏"/"新能源"等关键词作为模拟行业。
 * <p>输出：标准的 {@link AnalysisResultDTO}，<code>result</code> 字段是 6 维度结构
 * （overview / marketSize / chain / leading / trends / risks）+ chainNodes + leadingCompanies + references。
 * <p>激活条件：{@code aidsp.agent.mock-only=true}；为 false（默认）时由 {@link LlmIndustryAnalysisAgent} 接管。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aidsp.agent.mock-only", havingValue = "true")
public class IndustryAnalysisAgent implements AnalysisAgent {

    @Override
    public AnalysisType supports() {
        return AnalysisType.INDUSTRY;
    }

    @Override
    public AnalysisResultDTO run(AnalysisQueryRequest request) {
        long sleep = 1200L + ThreadLocalRandom.current().nextInt(801);
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String query = request == null || request.getQuery() == null ? "" : request.getQuery();
        IndustryTemplate tpl = detectTemplate(query);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("target", tpl.target);
        result.put("overview", buildOverview(tpl));
        result.put("marketSize", buildMarketSize(tpl));
        result.put("chain", buildChain(tpl));
        result.put("leading", buildLeading(tpl));
        result.put("trends", buildTrends(tpl));
        result.put("risks", buildRisks(tpl));
        result.put("chainNodes", buildChainNodes(tpl));
        result.put("leadingCompanies", buildLeadingCompanies(tpl));
        result.put("references", buildReferences(tpl));

        log.info("[IndustryAnalysisAgent] processed query='{}' target='{}' in {}ms",
                query, tpl.target, sleep);

        return AnalysisResultDTO.builder()
                .analysisType(AnalysisType.INDUSTRY)
                .result(result)
                .tookMs(sleep)
                .build();
    }

    // -------------------- 维度构建 --------------------

    private Map<String, Object> buildOverview(IndustryTemplate t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", "行业概况");
        m.put("icon", "AppstoreOutlined");
        m.put("color", "blue");
        m.put("summary", t.target + " 是当前宏观经济与产业升级中的关键赛道，覆盖原材料、核心制造到终端应用全链路。");
        m.put("keyPoints", List.of(
                "所属国民经济大类：" + t.gbCategory,
                "产业链定位：" + t.positioning,
                "近 3 年行业景气度：扩张期，订单与产能同步提升",
                "政策导向：受国家" + t.policy + "重点支持"
        ));
        return m;
    }

    private Map<String, Object> buildMarketSize(IndustryTemplate t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", "市场空间");
        m.put("icon", "RiseOutlined");
        m.put("color", "green");
        m.put("summary", "行业整体规模处于快速扩张通道，结构性机会集中于高景气细分。");
        m.put("keyPoints", List.of(
                "全球市场规模约 " + t.marketSize + " 亿元，CAGR 维持双位数",
                "国内市场份额占全球 " + t.chinaShare + "，本土供应链优势显著",
                "未来 3 年增量主要来自下游高端制造与出海需求"
        ));
        m.put("metrics", Map.of(
                "国内市场规模", t.domesticSize + " 亿元",
                "复合增速(CAGR)", t.cagr + "%",
                "市场集中度(CR5)", t.cr5 + "%"
        ));
        return m;
    }

    private Map<String, Object> buildChain(IndustryTemplate t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", "产业链结构");
        m.put("icon", "BranchesOutlined");
        m.put("color", "purple");
        m.put("summary", "产业链可拆分为上游原料、中游核心制造、下游应用三大环节，议价能力沿链向下游集中。");
        m.put("keyPoints", List.of(
                "上游：原料 / 设备供给端，技术壁垒高，国产替代空间大",
                "中游：核心制造环节，集中度提升，CR5 持续上行",
                "下游：应用场景快速扩张，价值链向服务端延伸"
        ));
        return m;
    }

    private Map<String, Object> buildLeading(IndustryTemplate t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", "龙头企业");
        m.put("icon", "TrophyOutlined");
        m.put("color", "gold");
        m.put("summary", "龙头公司在规模、技术与生态三方面具备显著领先优势，是行业景气度的核心受益方。");
        m.put("keyPoints", List.of(
                "头部 1-2 家占据主要市场份额，呈现赢者通吃格局",
                "第二梯队企业凭借差异化技术或区域市场建立护城河",
                "新进入者通过新工艺 / 新场景实现弯道超车"
        ));
        return m;
    }

    private Map<String, Object> buildTrends(IndustryTemplate t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", "未来趋势");
        m.put("icon", "RocketOutlined");
        m.put("color", "cyan");
        m.put("summary", "技术迭代、政策推动与出海共振将主导未来 3-5 年的行业演化路径。");
        m.put("keyPoints", List.of(
                "技术：核心工艺持续突破，单位成本逐年下降",
                "政策：双碳/数字化等政策红利持续释放",
                "出海：国产供应链向海外市场延伸，本土品牌国际化加速",
                "整合：行业进入并购整合期，头部公司估值溢价"
        ));
        return m;
    }

    private Map<String, Object> buildRisks(IndustryTemplate t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", "风险分析");
        m.put("icon", "WarningOutlined");
        m.put("color", "red");
        m.put("summary", "需重点关注上游价格波动、贸易摩擦及技术替代风险。");
        m.put("keyPoints", List.of(
                "上游：原材料价格波动可能压制中游毛利",
                "需求：宏观经济周期影响下游资本开支节奏",
                "政策：海外贸易/合规风险需持续跟踪",
                "技术：新一代工艺对现有产能存在替代风险"
        ));
        return m;
    }

    private List<Map<String, Object>> buildChainNodes(IndustryTemplate t) {
        return List.of(
                node("上游-原料/设备", "UPSTREAM", "原材料、关键设备及核心零部件供应", t.upstream),
                node("中游-核心制造", "MIDSTREAM", "组件/整机/系统集成与解决方案", t.midstream),
                node("下游-应用与服务", "DOWNSTREAM", "终端应用、运营服务与渠道", t.downstream)
        );
    }

    private Map<String, Object> node(String name, String type, String description, String reps) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("type", type);
        m.put("description", description);
        m.put("representatives", reps);
        return m;
    }

    private List<Map<String, Object>> buildLeadingCompanies(IndustryTemplate t) {
        return List.of(
                companyMap(t.lead1, "600000", 18.5, "全球龙头", "全球市场份额第一"),
                companyMap(t.lead2, "600001", 12.3, "国内龙头", "技术领先，国内市占率第一"),
                companyMap(t.lead3, "600002", 7.8, "高速成长", "新兴细分赛道领跑者"),
                companyMap(t.lead4, "600003", 5.2, "差异化竞争", "区域市场龙头")
        );
    }

    private Map<String, Object> companyMap(String name, String stock, double share, String tag, String desc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("stockCode", stock);
        m.put("marketShare", share);
        m.put("tag", tag);
        m.put("description", desc);
        return m;
    }

    private List<Map<String, Object>> buildReferences(IndustryTemplate t) {
        return List.of(
                ref("《" + t.target + "产业白皮书》", "https://example.com/whitepaper", t.target + " 全产业链深度研究。"),
                ref(t.target + "行业 2025 年度报告", "https://example.com/annual", "行业规模、增速与竞争格局。"),
                ref(t.target + "政策汇编", "https://example.com/policy", "国家与地方层面相关政策汇总。")
        );
    }

    private Map<String, Object> ref(String title, String url, String snippet) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", title);
        m.put("url", url);
        m.put("snippet", snippet);
        return m;
    }

    // -------------------- 行业模板识别 --------------------

    private IndustryTemplate detectTemplate(String query) {
        if (query == null || query.isBlank()) {
            return IndustryTemplate.defaults();
        }
        // 关键字匹配：按顺序返回
        if (contains(query, "光伏", "硅料", "隆基")) return IndustryTemplate.photovoltaic();
        if (contains(query, "锂电", "电池", "储能", "宁德")) return IndustryTemplate.lithiumBattery();
        if (contains(query, "新能源车", "新能源", "汽车", "比亚迪")) return IndustryTemplate.ev();
        if (contains(query, "半导体", "芯片", "集成电路", "IC")) return IndustryTemplate.semiconductor();
        if (contains(query, "医药", "生物", "制药")) return IndustryTemplate.biotech();
        if (contains(query, "互联网", "软件", "SaaS", "AI", "人工智能")) return IndustryTemplate.internet();
        if (contains(query, "金融", "银行", "证券", "保险")) return IndustryTemplate.finance();
        return IndustryTemplate.custom(query);
    }

    private boolean contains(String query, String... keys) {
        for (String k : keys) {
            if (query.contains(k)) return true;
        }
        return false;
    }

    /** 行业模板（避免把领域知识散落在 if 分支里）。 */
    private static final class IndustryTemplate {
        final String target;
        final String gbCategory;
        final String positioning;
        final String policy;
        final String marketSize;
        final String chinaShare;
        final String domesticSize;
        final String cagr;
        final String cr5;
        final String upstream;
        final String midstream;
        final String downstream;
        final String lead1;
        final String lead2;
        final String lead3;
        final String lead4;

        private IndustryTemplate(Builder b) {
            this.target = b.target;
            this.gbCategory = b.gbCategory;
            this.positioning = b.positioning;
            this.policy = b.policy;
            this.marketSize = b.marketSize;
            this.chinaShare = b.chinaShare;
            this.domesticSize = b.domesticSize;
            this.cagr = b.cagr;
            this.cr5 = b.cr5;
            this.upstream = b.upstream;
            this.midstream = b.midstream;
            this.downstream = b.downstream;
            this.lead1 = b.lead1;
            this.lead2 = b.lead2;
            this.lead3 = b.lead3;
            this.lead4 = b.lead4;
        }

        static IndustryTemplate defaults() {
            return new Builder()
                    .target("目标行业")
                    .gbCategory("制造业 - 综合")
                    .positioning("中游核心制造")
                    .policy("双碳与数字化")
                    .marketSize("8000")
                    .chinaShare("35%")
                    .domesticSize("2800")
                    .cagr("12.5")
                    .cr5("45")
                    .upstream("原材料 / 设备厂商")
                    .midstream("核心制造 / 集成")
                    .downstream("应用 / 渠道 / 服务")
                    .lead1("龙头企业 A")
                    .lead2("龙头企业 B")
                    .lead3("新兴龙头 C")
                    .lead4("区域龙头 D")
                    .build();
        }

        static IndustryTemplate photovoltaic() {
            return new Builder()
                    .target("光伏")
                    .gbCategory("制造业 / 电气机械")
                    .positioning("中游核心制造")
                    .policy("双碳与新能源")
                    .marketSize("12000")
                    .chinaShare("80%")
                    .domesticSize("9500")
                    .cagr("15.2")
                    .cr5("65")
                    .upstream("硅料 / 硅片 / 银浆 / 设备")
                    .midstream("电池片 / 组件 / 逆变器")
                    .downstream("电站 / 分布式 / 户用")
                    .lead1("隆基绿能")
                    .lead2("通威股份")
                    .lead3("晶科能源")
                    .lead4("天合光能")
                    .build();
        }

        static IndustryTemplate lithiumBattery() {
            return new Builder()
                    .target("锂离子电池")
                    .gbCategory("制造业 / 电气机械 - 电池")
                    .positioning("中游核心制造")
                    .policy("双碳与新能源")
                    .marketSize("15000")
                    .chinaShare("70%")
                    .domesticSize("10500")
                    .cagr("22.4")
                    .cr5("72")
                    .upstream("锂矿 / 钴矿 / 镍矿 / 正负极材料")
                    .midstream("电芯 / 模组 / PACK / 储能系统")
                    .downstream("新能源汽车 / 储能 / 消费电子")
                    .lead1("宁德时代")
                    .lead2("比亚迪")
                    .lead3("亿纬锂能")
                    .lead4("国轩高科")
                    .build();
        }

        static IndustryTemplate ev() {
            return new Builder()
                    .target("新能源汽车")
                    .gbCategory("制造业 / 汽车")
                    .positioning("中下游集成")
                    .policy("双碳与汽车产业升级")
                    .marketSize("20000")
                    .chinaShare("60%")
                    .domesticSize("12000")
                    .cagr("25.8")
                    .cr5("58")
                    .upstream("锂电 / 电机 / 电控 / 芯片")
                    .midstream("整车 / 平台 / 智能驾驶")
                    .downstream("出行服务 / 充电 / 二手车")
                    .lead1("比亚迪")
                    .lead2("特斯拉中国")
                    .lead3("蔚来汽车")
                    .lead4("小鹏汽车")
                    .build();
        }

        static IndustryTemplate semiconductor() {
            return new Builder()
                    .target("半导体")
                    .gbCategory("制造业 / 电子器件 - 集成电路")
                    .positioning("中游核心制造")
                    .policy("科技自立自强")
                    .marketSize("18000")
                    .chinaShare("25%")
                    .domesticSize("4500")
                    .cagr("11.0")
                    .cr5("38")
                    .upstream("硅片 / 光刻胶 / 设备 / EDA")
                    .midstream("设计 / 制造 / 封测")
                    .downstream("消费电子 / 通信 / 汽车 / AI 算力")
                    .lead1("中芯国际")
                    .lead2("韦尔股份")
                    .lead3("北方华创")
                    .lead4("长电科技")
                    .build();
        }

        static IndustryTemplate biotech() {
            return new Builder()
                    .target("生物医药")
                    .gbCategory("制造业 / 医药 - 生物制品")
                    .positioning("中游核心制造")
                    .policy("健康中国 2030")
                    .marketSize("9000")
                    .chinaShare("30%")
                    .domesticSize("2700")
                    .cagr("9.5")
                    .cr5("32")
                    .upstream("原料药 / 试剂 / 实验动物")
                    .midstream("化药 / 生物药 / 中药 / 器械")
                    .downstream("医院 / 药店 / 互联网医疗 / 出口")
                    .lead1("恒瑞医药")
                    .lead2("迈瑞医疗")
                    .lead3("药明康德")
                    .lead4("智飞生物")
                    .build();
        }

        static IndustryTemplate internet() {
            return new Builder()
                    .target("互联网与软件")
                    .gbCategory("信息传输 / 软件信息技术服务")
                    .positioning("中下游平台型")
                    .policy("数字经济与 AI+")
                    .marketSize("22000")
                    .chinaShare("45%")
                    .domesticSize("9900")
                    .cagr("13.5")
                    .cr5("68")
                    .upstream("云基础设施 / 芯片 / 数据要素")
                    .midstream("SaaS / PaaS / 算法平台")
                    .downstream("企业服务 / 消费互联网 / 出海")
                    .lead1("科大讯飞")
                    .lead2("金山办公")
                    .lead3("用友网络")
                    .lead4("广联达")
                    .build();
        }

        static IndustryTemplate finance() {
            return new Builder()
                    .target("金融业")
                    .gbCategory("金融业 - 综合")
                    .positioning("中下游服务")
                    .policy("金融监管与稳慎创新")
                    .marketSize("26000")
                    .chinaShare("38%")
                    .domesticSize("9900")
                    .cagr("7.8")
                    .cr5("55")
                    .upstream("金融数据 / 风险模型 / IT 基础设施")
                    .midstream("银行 / 证券 / 保险 / 资管")
                    .downstream("个人 / 机构 / 普惠 / 跨境")
                    .lead1("工商银行")
                    .lead2("招商银行")
                    .lead3("中信证券")
                    .lead4("中国平安")
                    .build();
        }

        static IndustryTemplate custom(String q) {
            String shortQ = q.length() > 12 ? q.substring(0, 12) : q;
            return new Builder()
                    .target(shortQ)
                    .gbCategory("综合分类")
                    .positioning("中游核心环节")
                    .policy("产业升级与创新驱动")
                    .marketSize("6500")
                    .chinaShare("30%")
                    .domesticSize("2000")
                    .cagr("10.0")
                    .cr5("40")
                    .upstream("原材料 / 设备 / 技术服务")
                    .midstream("核心制造 / 集成 / 平台")
                    .downstream("应用 / 渠道 / 服务")
                    .lead1(shortQ + "-龙头 A")
                    .lead2(shortQ + "-龙头 B")
                    .lead3(shortQ + "-新兴 C")
                    .lead4(shortQ + "-区域 D")
                    .build();
        }

        static final class Builder {
            String target, gbCategory, positioning, policy;
            String marketSize, chinaShare, domesticSize, cagr, cr5;
            String upstream, midstream, downstream;
            String lead1, lead2, lead3, lead4;

            Builder target(String v) { this.target = v; return this; }
            Builder gbCategory(String v) { this.gbCategory = v; return this; }
            Builder positioning(String v) { this.positioning = v; return this; }
            Builder policy(String v) { this.policy = v; return this; }
            Builder marketSize(String v) { this.marketSize = v; return this; }
            Builder chinaShare(String v) { this.chinaShare = v; return this; }
            Builder domesticSize(String v) { this.domesticSize = v; return this; }
            Builder cagr(String v) { this.cagr = v; return this; }
            Builder cr5(String v) { this.cr5 = v; return this; }
            Builder upstream(String v) { this.upstream = v; return this; }
            Builder midstream(String v) { this.midstream = v; return this; }
            Builder downstream(String v) { this.downstream = v; return this; }
            Builder lead1(String v) { this.lead1 = v; return this; }
            Builder lead2(String v) { this.lead2 = v; return this; }
            Builder lead3(String v) { this.lead3 = v; return this; }
            Builder lead4(String v) { this.lead4 = v; return this; }

            IndustryTemplate build() { return new IndustryTemplate(this); }
        }
    }
}
