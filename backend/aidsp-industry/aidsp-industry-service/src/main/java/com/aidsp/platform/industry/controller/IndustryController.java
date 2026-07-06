package com.aidsp.platform.industry.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 行业模块健康检查端点。
 */
@RestController
@RequestMapping("/api/v1/industries")
public class IndustryController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "industry", "status", "UP");
    }
}
