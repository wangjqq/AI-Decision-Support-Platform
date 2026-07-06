package com.aidsp.platform.knowledge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 知识库模块健康检查端点。
 */
@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "knowledge", "status", "UP");
    }
}
