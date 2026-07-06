package com.aidsp.platform.company.service;

import com.aidsp.platform.company.api.CompanyCreateRequest;
import com.aidsp.platform.company.api.CompanyUpdateRequest;
import com.aidsp.platform.company.api.CompanyVO;
import com.aidsp.platform.company.entity.Company;
import com.aidsp.platform.company.repository.CompanyRepository;
import com.aidsp.platform.core.api.PageResponse;
import com.aidsp.platform.core.exception.BusinessException;
import com.aidsp.platform.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公司服务实现。
 * <p>Dubbo in-JVM 服务，对外暴露 CRUD + 分页接口。
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class CompanyServiceImpl implements com.aidsp.platform.company.api.CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    public PageResponse<CompanyVO> page(Long page, Long size, String keyword, Long industryId) {
        PageResponse<Company> p = companyRepository.page(page, size, keyword, industryId);
        List<CompanyVO> list = p.getList().stream().map(companyMapper::toVO).toList();
        return PageResponse.<CompanyVO>builder()
                .list(list)
                .total(p.getTotal())
                .page(p.getPage())
                .size(p.getSize())
                .pages(p.getPages())
                .build();
    }

    @Override
    public CompanyVO getById(Long id) {
        return companyRepository.findById(id)
                .map(companyMapper::toVO)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
    }

    @Override
    public CompanyVO create(CompanyCreateRequest request) {
        validateUnique(request.getName(), request.getUscc(), null);
        LocalDateTime now = LocalDateTime.now();
        Company c = Company.builder()
                .name(request.getName())
                .uscc(request.getUscc())
                .industryId(request.getIndustryId())
                .industryName(industryNameOf(request.getIndustryId()))
                .mainBusiness(request.getMainBusiness())
                .address(request.getAddress())
                .establishedAt(request.getEstablishedAt())
                .description(request.getDescription())
                .createdAt(now)
                .updatedAt(now)
                .build();
        Company saved = companyRepository.save(c);
        log.info("[CompanyService] created company id={}, name={}", saved.getId(), saved.getName());
        return companyMapper.toVO(saved);
    }

    @Override
    public CompanyVO update(Long id, CompanyUpdateRequest request) {
        Company existing = companyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
        validateUnique(request.getName(), request.getUscc(), id);
        existing.setName(request.getName());
        existing.setUscc(request.getUscc());
        existing.setIndustryId(request.getIndustryId());
        existing.setIndustryName(industryNameOf(request.getIndustryId()));
        existing.setMainBusiness(request.getMainBusiness());
        existing.setAddress(request.getAddress());
        existing.setEstablishedAt(request.getEstablishedAt());
        existing.setDescription(request.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());
        Company updated = companyRepository.update(existing)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));
        log.info("[CompanyService] updated company id={}, name={}", id, updated.getName());
        return companyMapper.toVO(updated);
    }

    @Override
    public void delete(Long id) {
        boolean removed = companyRepository.deleteById(id);
        if (!removed) {
            throw new BusinessException(ErrorCode.COMPANY_NOT_FOUND);
        }
        log.info("[CompanyService] deleted company id={}", id);
    }

    private void validateUnique(String name, String uscc, Long excludeId) {
        if (companyRepository.existsByName(name, excludeId)) {
            throw new BusinessException(ErrorCode.COMPANY_NAME_DUPLICATE);
        }
        if (companyRepository.existsByUscc(uscc, excludeId)) {
            throw new BusinessException(ErrorCode.COMPANY_CONFLICT, "统一社会信用代码已存在");
        }
    }

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
}
