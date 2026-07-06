package com.aidsp.platform.report.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 报告模块健康检查端点。
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "report", "status", "UP");
    }
}
