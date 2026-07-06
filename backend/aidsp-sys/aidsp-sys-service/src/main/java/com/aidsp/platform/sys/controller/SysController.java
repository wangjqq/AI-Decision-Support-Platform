package com.aidsp.platform.sys.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统模块健康检查端点。
 */
@RestController
@RequestMapping("/api/v1/sys")
public class SysController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "sys", "status", "UP");
    }
}
