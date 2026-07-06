package com.aidsp.platform.rag.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * RAG 模块健康检查端点。
 */
@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "rag", "status", "UP");
    }
}
