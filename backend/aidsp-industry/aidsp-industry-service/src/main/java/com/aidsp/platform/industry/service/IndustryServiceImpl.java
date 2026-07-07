package com.aidsp.platform.industry.service;

import com.aidsp.platform.industry.api.IndustryCreateRequest;
import com.aidsp.platform.industry.api.IndustryUpdateRequest;
import com.aidsp.platform.industry.api.IndustryVO;
import com.aidsp.platform.industry.entity.Industry;
import com.aidsp.platform.industry.repository.IndustryRepository;
import com.aidsp.platform.core.api.PageResponse;
import com.aidsp.platform.core.exception.BusinessException;
import com.aidsp.platform.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 行业服务实现。
 * <p>Dubbo in-JVM 服务，对外暴露 CRUD + 分页接口。
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class IndustryServiceImpl implements com.aidsp.platform.industry.api.IndustryService {

    private final IndustryRepository industryRepository;
    private final IndustryMapper industryMapper;

    @Override
    public PageResponse<IndustryVO> page(Long page, Long size, String keyword, Integer level, Long parentId) {
        PageResponse<Industry> p = industryRepository.page(page, size, keyword, level, parentId);
        List<IndustryVO> list = p.getList().stream().map(industryMapper::toVO).toList();
        return PageResponse.<IndustryVO>builder()
                .list(list)
                .total(p.getTotal())
                .page(p.getPage())
                .size(p.getSize())
                .pages(p.getPages())
                .build();
    }

    @Override
    public IndustryVO getById(Long id) {
        return industryRepository.findById(id)
                .map(industryMapper::toVO)
                .orElseThrow(() -> new BusinessException(ErrorCode.INDUSTRY_NOT_FOUND));
    }

    @Override
    public IndustryVO create(IndustryCreateRequest request) {
        validateUnique(request.getCode(), request.getName(), null);
        validateParent(request.getParentId());
        LocalDateTime now = LocalDateTime.now();
        Industry ind = Industry.builder()
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .level(request.getLevel())
                .parentId(request.getParentId())
                .description(request.getDescription())
                .tags(request.getTags())
                .status(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
        Industry saved = industryRepository.save(ind);
        log.info("[IndustryService] created industry id={}, code={}, name={}",
                saved.getId(), saved.getCode(), saved.getName());
        return industryMapper.toVO(saved);
    }

    @Override
    public IndustryVO update(Long id, IndustryUpdateRequest request) {
        Industry existing = industryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INDUSTRY_NOT_FOUND));
        if (industryRepository.existsByName(request.getName(), id)) {
            throw new BusinessException(ErrorCode.INDUSTRY_NAME_DUPLICATE);
        }
        validateParent(request.getParentId());
        existing.setName(request.getName().trim());
        existing.setLevel(request.getLevel());
        existing.setParentId(request.getParentId());
        existing.setDescription(request.getDescription());
        existing.setTags(request.getTags());
        existing.setUpdatedAt(LocalDateTime.now());
        Industry updated = industryRepository.update(existing)
                .orElseThrow(() -> new BusinessException(ErrorCode.INDUSTRY_NOT_FOUND));
        log.info("[IndustryService] updated industry id={}, name={}", id, updated.getName());
        return industryMapper.toVO(updated);
    }

    @Override
    public void delete(Long id) {
        if (industryRepository.hasChildren(id)) {
            throw new BusinessException(ErrorCode.INDUSTRY_CONFLICT, "存在子行业，无法删除");
        }
        boolean removed = industryRepository.deleteById(id);
        if (!removed) {
            throw new BusinessException(ErrorCode.INDUSTRY_NOT_FOUND);
        }
        log.info("[IndustryService] deleted industry id={}", id);
    }

    private void validateUnique(String code, String name, Long excludeId) {
        if (industryRepository.existsByCode(code, excludeId)) {
            throw new BusinessException(ErrorCode.INDUSTRY_CODE_DUPLICATE);
        }
        if (industryRepository.existsByName(name, excludeId)) {
            throw new BusinessException(ErrorCode.INDUSTRY_NAME_DUPLICATE);
        }
    }

    private void validateParent(Long parentId) {
        if (parentId == null) {
            return;
        }
        industryRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INDUSTRY_NOT_FOUND, "父行业不存在: " + parentId));
    }
}
