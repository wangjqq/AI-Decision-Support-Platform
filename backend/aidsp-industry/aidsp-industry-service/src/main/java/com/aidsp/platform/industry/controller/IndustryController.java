package com.aidsp.platform.industry.controller;

import com.aidsp.platform.core.api.PageResponse;
import com.aidsp.platform.industry.api.IndustryAnalysisHistoryItemVO;
import com.aidsp.platform.industry.api.IndustryAnalysisRequest;
import com.aidsp.platform.industry.api.IndustryAnalysisResultVO;
import com.aidsp.platform.industry.api.IndustryCreateRequest;
import com.aidsp.platform.industry.api.IndustryUpdateRequest;
import com.aidsp.platform.industry.api.IndustryVO;
import com.aidsp.platform.industry.service.IndustryAnalysisService;
import com.aidsp.platform.industry.service.IndustryServiceImpl;
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
 * 行业模块 REST 控制器。
 * <p>所有方法由 {@code GlobalRestResponseAdvice} 自动包装为 {@code RestResponse<T>}。
 */
@RestController
@RequestMapping("/api/v1/industries")
@RequiredArgsConstructor
public class IndustryController {

    private final IndustryServiceImpl industryService;
    private final IndustryAnalysisService analysisService;

    // -------------------- CRUD --------------------

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "industry", "status", "UP");
    }

    @GetMapping
    public PageResponse<IndustryVO> page(
            @RequestParam(required = false, defaultValue = "1") Long page,
            @RequestParam(required = false, defaultValue = "20") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Long parentId) {
        return industryService.page(page, size, keyword, level, parentId);
    }

    @GetMapping("/{id}")
    public IndustryVO getById(@PathVariable Long id) {
        return industryService.getById(id);
    }

    @PostMapping
    public IndustryVO create(@Valid @RequestBody IndustryCreateRequest request) {
        return industryService.create(request);
    }

    @PutMapping("/{id}")
    public IndustryVO update(@PathVariable Long id, @Valid @RequestBody IndustryUpdateRequest request) {
        return industryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        industryService.delete(id);
    }

    // -------------------- 分析 --------------------

    @PostMapping("/{id}/analyses")
    public IndustryAnalysisResultVO analyze(@PathVariable Long id,
                                            @Valid @RequestBody(required = false) IndustryAnalysisRequest request) {
        return analysisService.analyze(id, request);
    }

    @GetMapping("/{id}/analyses")
    public PageResponse<IndustryAnalysisHistoryItemVO> listAnalyses(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long page,
            @RequestParam(required = false, defaultValue = "20") Long size) {
        return analysisService.pageHistory(id, page, size);
    }

    @GetMapping("/{id}/analyses/{analysisId}")
    public IndustryAnalysisResultVO getAnalysis(@PathVariable Long id, @PathVariable String analysisId) {
        return analysisService.getById(id, analysisId);
    }

    @DeleteMapping("/{id}/analyses/{analysisId}")
    public void deleteAnalysis(@PathVariable Long id, @PathVariable String analysisId) {
        analysisService.deleteAnalysis(id, analysisId);
    }
}
