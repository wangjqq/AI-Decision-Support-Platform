package com.aidsp.platform.company.controller;

import com.aidsp.platform.company.api.CompanyAnalysisHistoryItemVO;
import com.aidsp.platform.company.api.CompanyAnalysisRequest;
import com.aidsp.platform.company.api.CompanyAnalysisResultVO;
import com.aidsp.platform.company.api.CompanyCreateRequest;
import com.aidsp.platform.company.api.CompanyUpdateRequest;
import com.aidsp.platform.company.api.CompanyVO;
import com.aidsp.platform.company.service.CompanyAnalysisService;
import com.aidsp.platform.company.service.CompanyServiceImpl;
import com.aidsp.platform.core.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 公司模块 REST 控制器。
 * <p>所有方法由 {@code GlobalRestResponseAdvice} 自动包装为 {@code RestResponse<T>}。
 */
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyServiceImpl companyService;
    private final CompanyAnalysisService analysisService;

    // -------------------- CRUD --------------------

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "company", "status", "UP");
    }

    @GetMapping
    public PageResponse<CompanyVO> page(
            @RequestParam(required = false, defaultValue = "1") Long page,
            @RequestParam(required = false, defaultValue = "20") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long industryId) {
        return companyService.page(page, size, keyword, industryId);
    }

    @GetMapping("/{id}")
    public CompanyVO getById(@PathVariable Long id) {
        return companyService.getById(id);
    }

    @PostMapping
    public CompanyVO create(@Valid @RequestBody CompanyCreateRequest request) {
        return companyService.create(request);
    }

    @PutMapping("/{id}")
    public CompanyVO update(@PathVariable Long id, @Valid @RequestBody CompanyUpdateRequest request) {
        return companyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        companyService.delete(id);
    }

    // -------------------- 分析 --------------------

    @PostMapping("/{id}/analyses")
    public CompanyAnalysisResultVO analyze(@PathVariable Long id,
                                            @Valid @RequestBody(required = false) CompanyAnalysisRequest request) {
        return analysisService.analyze(id, request);
    }

    @GetMapping("/{id}/analyses")
    public PageResponse<CompanyAnalysisHistoryItemVO> listAnalyses(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long page,
            @RequestParam(required = false, defaultValue = "20") Long size) {
        return analysisService.pageHistory(id, page, size);
    }

    @GetMapping("/{id}/analyses/{analysisId}")
    public CompanyAnalysisResultVO getAnalysis(@PathVariable Long id, @PathVariable String analysisId) {
        return analysisService.getById(id, analysisId);
    }

    @DeleteMapping("/{id}/analyses/{analysisId}")
    public void deleteAnalysis(@PathVariable Long id, @PathVariable String analysisId) {
        analysisService.deleteAnalysis(id, analysisId);
    }
}
