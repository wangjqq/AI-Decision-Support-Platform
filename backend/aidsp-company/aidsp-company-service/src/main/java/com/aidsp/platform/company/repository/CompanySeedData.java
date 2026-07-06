package com.aidsp.platform.company.repository;

import com.aidsp.platform.company.entity.Company;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 公司样例数据。
 * <p>启动时写入 5 条样例公司，便于前端联调。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanySeedData {

    private final CompanyRepository repository;

    @PostConstruct
    public void seed() {
        seed("宁德时代新能源科技股份有限公司", "91350900161101155A", 1L,
                "锂离子电池、储能系统及电池回收", "福建省宁德市蕉城区漳湾镇新港路 1 号",
                LocalDate.of(2011, 12, 16), "全球领先的新能源创新科技公司");
        seed("比亚迪股份有限公司", "91440300192181804Y", 2L,
                "新能源汽车、动力电池、轨道交通、电子", "广东省深圳市坪山区比亚迪路 3009 号",
                LocalDate.of(1995, 2, 10), "横跨汽车、电池、电子和轨道交通四大产业");
        seed("隆基绿能科技股份有限公司", "91610100MA6TG3W07H", 3L,
                "单晶硅、硅片、电池组件、电站", "西安市经济技术开发区尚苑路 8369 号",
                LocalDate.of(2000, 2, 14), "全球最大的单晶硅光伏产品制造商");
        seed("科大讯飞股份有限公司", "9134010014921001XK", 4L,
                "智能语音、人工智能技术研究、软件及芯片", "合肥市高新开发区望江西路 666 号",
                LocalDate.of(1999, 12, 30), "中国智能语音和人工智能领军企业");
        seed("迈瑞医疗国际股份有限公司", "91440300192175737L", 5L,
                "生命信息与支持、IVD 体外诊断、医学影像", "深圳市南山区高新技术产业园区科技南十二路 22 号",
                LocalDate.of(1999, 1, 25), "中国领先的高科技医疗设备研发制造厂商");
        log.info("[CompanySeedData] seeded {} companies", 5);
    }

    private void seed(String name, String uscc, Long industryId, String mainBusiness,
                      String address, LocalDate establishedAt, String description) {
        LocalDateTime now = LocalDateTime.now();
        Company c = Company.builder()
                .name(name)
                .uscc(uscc)
                .industryId(industryId)
                .industryName(mapIndustryName(industryId))
                .mainBusiness(mainBusiness)
                .address(address)
                .establishedAt(establishedAt)
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .build();
        repository.putIfAbsent(c);
    }

    private String mapIndustryName(Long industryId) {
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
