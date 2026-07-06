package com.aidsp.platform.analysis.controller;

import com.aidsp.platform.analysis.api.AnalysisQueryRequest;
import com.aidsp.platform.analysis.api.AnalysisResultDTO;
import com.aidsp.platform.analysis.api.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 智能分析模块 Controller。
 */
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * 提交分析问题并获取结果。
     */
    @PostMapping("/query")
    public AnalysisResultDTO query(@Valid @RequestBody AnalysisQueryRequest request) {
        return analysisService.analyze(request);
    }

    /**
     * 健康检查。
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "analysis", "status", "UP");
    }
}
