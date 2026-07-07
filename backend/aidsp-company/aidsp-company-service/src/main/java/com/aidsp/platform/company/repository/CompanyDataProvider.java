package com.aidsp.platform.company.repository;

import com.aidsp.platform.company.entity.Company;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 公司真实数据提供器。
 * <p>为 MyBatis-Plus 仓储准备"真实企业"数据集：
 * 包含股票代码、细分行业、业务板块、财务核心指标等。
 * <p>字段扁平化以匹配 {@code company} 表（revenue / profit / financial_period 三列）；
 * <br>由 {@code CompanyVoConverter} 在读取时组装为 {@code CompanyFinancialVO}。
 */
public final class CompanyDataProvider {

    private CompanyDataProvider() {
    }

    /**
     * 内置真实样例企业（含 A 股上市公司）。
     * <p>按 {@code id} 升序；ID 由 DB AUTO_INCREMENT 分配，本字段仅用于按 id 定位逻辑读取。
     */
    public static List<Company> defaultCompanies() {
        return Arrays.asList(
                build(
                        "深圳市英维克科技股份有限公司",
                        "002837",
                        "91440300192174925W",
                        4L,
                        "液冷设备",
                        "机房精密温控、液冷散热系统、新能源汽车热管理",
                        Arrays.asList("机房温控", "液冷散热", "新能源热管理", "储能温控"),
                        "广东省深圳市龙华区观湖街道环观南路 268 号",
                        LocalDate.of(2005, 8, 15),
                        "国内领先的精密温控节能设备与解决方案提供商，机房精密空调、户外通信柜、新能源汽车热管理三大业务板块协同发展。",
                        new BigDecimal("4582000000"),
                        new BigDecimal("412000000"),
                        "2024"
                ),
                build(
                        "宁德时代新能源科技股份有限公司",
                        "300750",
                        "91350900161101155A",
                        1L,
                        "动力电池",
                        "锂离子电池、储能系统及电池回收",
                        Arrays.asList("动力电池", "储能电池", "电池回收", "电池材料"),
                        "福建省宁德市蕉城区漳湾镇新港路 1 号",
                        LocalDate.of(2011, 12, 16),
                        "全球领先的新能源创新科技公司，连续多年动力电池装机量位居全球第一。",
                        new BigDecimal("362000000000"),
                        new BigDecimal("50700000000"),
                        "2024"
                ),
                build(
                        "比亚迪股份有限公司",
                        "002594",
                        "91440300192181804Y",
                        2L,
                        "新能源汽车",
                        "新能源汽车、动力电池、轨道交通、电子",
                        Arrays.asList("新能源汽车", "动力电池", "轨道交通", "电子"),
                        "广东省深圳市坪山区比亚迪路 3009 号",
                        LocalDate.of(1995, 2, 10),
                        "横跨汽车、电池、电子和轨道交通四大产业，新能源汽车销量全球领先。",
                        new BigDecimal("777000000000"),
                        new BigDecimal("40200000000"),
                        "2024"
                ),
                build(
                        "隆基绿能科技股份有限公司",
                        "601012",
                        "91610100MA6TG3W07H",
                        3L,
                        "光伏",
                        "单晶硅、硅片、电池组件、电站",
                        Arrays.asList("单晶硅棒", "硅片", "电池组件", "光伏电站"),
                        "西安市经济技术开发区尚苑路 8369 号",
                        LocalDate.of(2000, 2, 14),
                        "全球最大的单晶硅光伏产品制造商，专注于太阳能光伏全产业链。",
                        new BigDecimal("825000000000"),
                        new BigDecimal("-8600000000"),
                        "2024"
                ),
                build(
                        "科大讯飞股份有限公司",
                        "002230",
                        "9134010014921001XK",
                        4L,
                        "人工智能",
                        "智能语音、人工智能技术研究、软件及芯片",
                        Arrays.asList("智能语音", "教育 AI", "智慧城市", "AI 开放平台"),
                        "合肥市高新开发区望江西路 666 号",
                        LocalDate.of(1999, 12, 30),
                        "中国智能语音和人工智能领军企业，在语音合成、语音识别等核心技术上保持国际领先。",
                        new BigDecimal("23400000000"),
                        new BigDecimal("960000000"),
                        "2024"
                ),
                build(
                        "迈瑞医疗国际股份有限公司",
                        "300760",
                        "91440300192175737L",
                        5L,
                        "医疗器械",
                        "生命信息与支持、IVD 体外诊断、医学影像",
                        Arrays.asList("监护与生命支持", "体外诊断", "医学影像", "微创外科"),
                        "深圳市南山区高新技术产业园区科技南十二路 22 号",
                        LocalDate.of(1999, 1, 25),
                        "中国领先的高科技医疗设备研发制造厂商，产品远销全球 190 多个国家和地区。",
                        new BigDecimal("39400000000"),
                        new BigDecimal("11700000000"),
                        "2024"
                )
        );
    }

    private static Company build(String name, String code, String uscc,
                                  Long industryId, String industry, String mainBusiness,
                                  List<String> business, String address, LocalDate establishedAt,
                                  String description, BigDecimal revenue, BigDecimal profit,
                                  String period) {
        return Company.builder()
                .name(name)
                .code(code)
                .uscc(uscc)
                .industryId(industryId)
                .industryName(mapIndustryName(industryId))
                .industry(industry)
                .mainBusiness(mainBusiness)
                .business(business)
                .address(address)
                .establishedAt(establishedAt)
                .description(description)
                .revenue(revenue)
                .profit(profit)
                .financialPeriod(period)
                .build();
    }

    private static String mapIndustryName(Long industryId) {
        if (industryId == null) {
            return "-";
        }
        return switch (industryId.intValue()) {
            case 1 -> "锂离子电池";
            case 2 -> "新能源汽车";
            case 3 -> "光伏";
            case 4 -> "人工智能";
            case 5 -> "医疗器械";
            default -> "其他";
        };
    }
}
