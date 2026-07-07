package com.aidsp.platform.company.service;

import com.aidsp.platform.company.api.CompanyCreateRequest;
import com.aidsp.platform.company.api.CompanyFinancialVO;
import com.aidsp.platform.company.api.CompanyUpdateRequest;
import com.aidsp.platform.company.api.CompanyVO;
import com.aidsp.platform.company.entity.Company;
import com.aidsp.platform.company.repository.CompanyMapper;
import com.aidsp.platform.core.api.PageResponse;
import com.aidsp.platform.core.exception.BusinessException;
import com.aidsp.platform.core.exception.ErrorCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * 公司服务实现。
 * <p>基于 MyBatis-Plus {@link CompanyMapper} 持久化；ID 由数据库 AUTO_INCREMENT 生成，
 * <br>{@code createdAt / updatedAt} 由 {@code AutoFillMetaObjectHandler} 自动填充。
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class CompanyServiceImpl implements com.aidsp.platform.company.api.CompanyService {

    private final CompanyMapper companyMapper;
    private final CompanyVoConverter companyVoConverter;

    @Override
    public PageResponse<CompanyVO> page(Long page, Long size, String keyword, Long industryId) {
        long p = page == null || page <= 0 ? 1L : page;
        long s = size == null || size <= 0 ? 20L : size;
        String kw = keyword == null ? "" : keyword.trim();

        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        if (industryId != null) {
            wrapper.eq(Company::getIndustryId, industryId);
        }
        if (!kw.isEmpty()) {
            wrapper.like(Company::getName, kw);
        }
        wrapper.orderByAsc(Company::getId);

        Page<Company> mpPage = companyMapper.selectPage(new Page<>(p, s), wrapper);
        List<CompanyVO> list = mpPage.getRecords().stream().map(companyVoConverter::toVO).toList();

        return PageResponse.<CompanyVO>builder()
                .list(list)
                .total(mpPage.getTotal())
                .page(p)
                .size(s)
                .pages(mpPage.getPages())
                .build();
    }

    @Override
    public CompanyVO getById(Long id) {
        Company c = companyMapper.selectById(id);
        if (c == null) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }
        return companyVoConverter.toVO(c);
    }

    @Override
    public CompanyVO create(CompanyCreateRequest request) {
        validateUnique(request.getName(), request.getUscc(), null);
        Company c = Company.builder()
                .name(request.getName())
                .code(request.getCode())
                .uscc(request.getUscc())
                .industryId(request.getIndustryId())
                .industryName(industryNameOf(request.getIndustryId()))
                .industry(request.getIndustry())
                .mainBusiness(request.getMainBusiness())
                .business(request.getBusiness())
                .address(request.getAddress())
                .establishedAt(request.getEstablishedAt())
                .description(request.getDescription())
                .revenue(safeRevenue(request.getFinancial()))
                .profit(safeProfit(request.getFinancial()))
                .financialPeriod(safePeriod(request.getFinancial()))
                .build();
        companyMapper.insert(c);
        log.info("[CompanyService] created company id={}, name={}", c.getId(), c.getName());
        return companyVoConverter.toVO(c);
    }

    @Override
    public CompanyVO update(Long id, CompanyUpdateRequest request) {
        Company existing = companyMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }
        validateUnique(request.getName(), request.getUscc(), id);
        existing.setName(request.getName());
        existing.setCode(request.getCode());
        existing.setUscc(request.getUscc());
        existing.setIndustryId(request.getIndustryId());
        existing.setIndustryName(industryNameOf(request.getIndustryId()));
        existing.setIndustry(request.getIndustry());
        existing.setMainBusiness(request.getMainBusiness());
        existing.setBusiness(request.getBusiness());
        existing.setAddress(request.getAddress());
        existing.setEstablishedAt(request.getEstablishedAt());
        existing.setDescription(request.getDescription());
        existing.setRevenue(safeRevenue(request.getFinancial()));
        existing.setProfit(safeProfit(request.getFinancial()));
        existing.setFinancialPeriod(safePeriod(request.getFinancial()));
        int updated = companyMapper.updateById(existing);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }
        log.info("[CompanyService] updated company id={}, name={}", id, existing.getName());
        return companyVoConverter.toVO(companyMapper.selectById(id));
    }

    @Override
    public void delete(Long id) {
        int removed = companyMapper.deleteById(id);
        if (removed == 0) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }
        log.info("[CompanyService] deleted company id={}", id);
    }

    // -------------------- helpers --------------------

    /** 唯一性校验：name / uscc 冲突。 */
    private void validateUnique(String name, String uscc, Long excludeId) {
        if (name != null && !name.isBlank()) {
            Long dup = companyMapper.selectCount(new LambdaQueryWrapper<Company>()
                    .eq(Company::getName, name)
                    .ne(excludeId != null, Company::getId, excludeId)
                    .last("LIMIT 1"));
            if (dup != null && dup > 0) {
                throw new BusinessException(ErrorCode.COMPANY_NAME_DUPLICATE);
            }
        }
        if (uscc != null && !uscc.isBlank()) {
            Long dup = companyMapper.selectCount(new LambdaQueryWrapper<Company>()
                    .eq(Company::getUscc, uscc)
                    .ne(excludeId != null, Company::getId, excludeId)
                    .last("LIMIT 1"));
            if (dup != null && dup > 0) {
                throw new BusinessException(ErrorCode.COMPANY_CONFLICT, "统一社会信用代码已存在");
            }
        }
    }

    /** MVP 阶段行业 id → 名称硬编码映射（后续接 industry 表 JOIN）。 */
    private String industryNameOf(Long industryId) {
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

    private java.math.BigDecimal safeRevenue(CompanyFinancialVO f) {
        return f == null ? null : f.getRevenue();
    }

    private java.math.BigDecimal safeProfit(CompanyFinancialVO f) {
        return f == null ? null : f.getProfit();
    }

    private String safePeriod(CompanyFinancialVO f) {
        return f == null ? null : f.getPeriod();
    }

    @SuppressWarnings("unused")
    private String lower(String s) {
        return s == null ? null : s.toLowerCase(Locale.ROOT);
    }
}
