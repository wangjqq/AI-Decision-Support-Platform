package com.aidsp.platform.company.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 公司模块健康检查端点。
 */
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "company", "status", "UP");
    }
}
