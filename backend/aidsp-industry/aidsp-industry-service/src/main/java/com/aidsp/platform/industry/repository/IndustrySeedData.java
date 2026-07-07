package com.aidsp.platform.industry.repository;

import com.aidsp.platform.industry.entity.Industry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
/**
 * 行业样例数据。
 * <p>启动时按 GB/T 4754 写入若干样例行业，便于前端联调。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndustrySeedData {

    private final IndustryRepository repository;

    @PostConstruct
    public void seed() {
        // 门类
        seed(null, "A", "农、林、牧、渔业", 1, "以农业、林业、畜牧业、渔业为主营的产业集合。",
                "第一产业,基础产业");
        seed(null, "B", "采矿业", 1, "煤炭、石油天然气、金属与非金属矿的开采与洗选。",
                "资源,上游");
        seed(null, "C", "制造业", 1, "覆盖装备、电子、化工、冶金等广泛的制造产业。",
                "实体经济,中游");
        seed(null, "D", "电力、热力、燃气及水生产和供应业", 1, "能源生产、输配与公共事业服务。",
                "公用事业,能源");
        seed(null, "E", "建筑业", 1, "房屋建筑、土木工程、建筑安装与装饰。",
                "基建,地产");
        seed(null, "F", "批发和零售业", 1, "商品批发、零售、贸易与电商。",
                "消费,商贸");
        seed(null, "G", "交通运输、仓储和邮政业", 1, "铁路、公路、水运、航空与物流仓储。",
                "物流,交通");
        seed(null, "I", "信息传输、软件和信息技术服务业", 1, "电信、互联网、软件与信息技术服务。",
                "数字经济,TMT");
        seed(null, "J", "金融业", 1, "银行、证券、保险与金融服务。",
                "金融,资本");
        seed(null, "K", "房地产业", 1, "房地产开发、经营与物业管理。",
                "地产,不动产");
        seed(null, "M", "科学研究和技术服务业", 1, "研发、专业技术服务与科技推广。",
                "科研,服务");
        seed(null, "N", "水利、环境和公共设施管理业", 1, "水利、环境治理与公共设施。",
                "环保,公共");
        seed(null, "O", "居民服务、修理和其他服务业", 1, "居民日常生活相关的服务。",
                "生活服务");
        seed(null, "P", "教育", 1, "学前教育到高等教育的综合教育服务。",
                "教育");
        seed(null, "Q", "卫生和社会工作", 1, "医疗、卫生与公共健康服务。",
                "医疗,健康");
        seed(null, "R", "文化、体育和娱乐业", 1, "文化、体育、传媒与娱乐。",
                "文娱,传媒");
        seed(null, "S", "公共管理、社会保障和社会组织", 1, "公共行政与社会保障。",
                "公共管理");
        seed(null, "T", "国际组织", 1, "国际组织与跨国事务。", "国际");

        // 大类（制造业下的几个常见大类）
        seed("C", "C26", "化学原料和化学制品制造业", 2,
                "基础化工原料、肥料、涂料、合成材料等。", "化工,材料");
        seed("C", "C27", "医药制造业", 2,
                "化学药品原料药与制剂、中药与生物药品。", "医药,生物");
        seed("C", "C36", "汽车制造业", 2,
                "整车、零部件、新能源汽车与智能网联汽车。", "汽车,出行");
        seed("C", "C38", "电气机械和器材制造业", 2,
                "电机、电池、家用电器与输配电设备。", "电气,装备");
        seed("C", "C39", "计算机、通信和其他电子设备制造业", 2,
                "计算机、通信设备、芯片、半导体与消费电子。", "电子,半导体");
        // 信息传输、软件和信息技术服务业
        seed("I", "I62", "电信、广播电视和卫星传输服务", 2,
                "电信运营、广播电视与卫星传输。", "通信,运营商");
        seed("I", "I63", "互联网和相关服务", 2,
                "互联网平台、信息服务与软件交付。", "互联网,平台");
        seed("I", "I65", "软件和信息技术服务业", 2,
                "软件开发、信息系统集成与信息技术服务。", "软件,IT服务");
        // 金融业
        seed("J", "J66", "货币金融服务", 2, "中央银行与商业银行。", "银行");
        seed("J", "J67", "资本市场服务", 2, "证券、期货、基金与资产管理。", "证券,资管");
        seed("J", "J68", "保险业", 2, "人寿、财产与再保险。", "保险");

        // 中类（重点：聚焦公司页会引用到的行业）
        seed("C39", "C391", "计算机制造", 3, "整机制造、服务器与外设。", "计算机,服务器");
        seed("C39", "C397", "电子器件制造", 3, "半导体分立器件、集成电路与显示器件。", "芯片,半导体");
        seed("C36", "C361", "汽车整车制造", 3, "乘用车与商用车整车制造。", "整车");
        seed("C36", "C362", "汽车零部件及配件制造", 3, "动力总成、底盘与电子部件。", "零部件");
        seed("C38", "C384", "电池制造", 3, "锂离子电池、储能电池与消费类电池。", "锂电,储能");
        seed("C38", "C385", "家用电力器具制造", 3, "家电、厨电与小家电。", "家电,消费");
        seed("C26", "C261", "基础化学原料制造", 3, "无机与有机基础化工原料。", "基础化工");
        seed("C27", "C271", "化学药品原料药制造", 3, "原料药与中间体。", "原料药");
        seed("C27", "C276", "生物药品制品制造", 3, "疫苗、抗体与基因工程药物。", "生物药");
        seed("I63", "I631", "互联网信息服务", 3, "搜索引擎、社交与内容平台。", "互联网,内容");
        seed("I65", "I651", "软件开发", 3, "基础软件、应用软件与行业 SaaS。", "软件,SaaS");
        seed("J67", "J671", "证券市场服务", 3, "证券交易所、券商与投行。", "证券");
        seed("J68", "J681", "人寿保险", 3, "人身保险与年金。", "人寿保险");
        seed("J68", "J682", "财产保险", 3, "车险、财产险与责任险。", "财险");

        // 小类
        seed("C397", "C3971", "半导体分立器件制造", 4, "二极管、晶体管与功率器件。", "分立器件");
        seed("C397", "C3972", "集成电路制造", 4, "逻辑芯片、存储芯片与模拟芯片。", "IC,芯片");
        seed("C384", "C3841", "锂离子电池制造", 4, "动力电池、储能电池与消费电池。", "锂电,新能源");
        seed("C362", "C3620", "新能源汽车零部件制造", 4, "电驱、电控、电池包与热管理。", "新能源车,零部件");
        seed("C261", "C2611", "无机化学原料制造", 4, "酸、碱、盐与无机功能材料。", "无机化工");
        seed("C271", "C2710", "化学药品原料药制造（综合）", 4, "综合性原料药生产。", "原料药,综合");
        seed("C276", "C2760", "生物制品制造（综合）", 4, "综合性生物制品。", "生物制品");
        seed("I651", "I6510", "软件开发（综合）", 4, "综合性软件研发与交付。", "软件,综合");
        seed("I631", "I6310", "互联网信息服务（综合）", 4, "综合性互联网信息服务。", "互联网,综合");
        seed("J671", "J6710", "证券市场服务（综合）", 4, "综合性证券市场服务。", "证券,综合");
        seed("J681", "J6810", "人寿保险（综合）", 4, "综合性人寿保险。", "人寿,综合");

        log.info("[IndustrySeedData] seeded {} industries", repository.count());
    }

    private void seed(String parentCode, String code, String name, int level,
                      String description, String tags) {
        Long parentId = parentCode == null ? null : lookupIdByCode(parentCode);
        LocalDateTime now = LocalDateTime.now();
        Industry ind = Industry.builder()
                .code(code)
                .name(name)
                .level(level)
                .parentId(parentId)
                .description(description)
                .tags(tags)
                .status(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
        repository.putIfAbsent(ind);
    }

    /**
     * 根据编码在已经写入的种子数据中查找 id（按编码精确匹配）。
     * 父行业必须先于子行业调用 seed。
     */
    private Long lookupIdByCode(String code) {
        return repository.findByCode(code).map(Industry::getId).orElse(null);
    }
}
