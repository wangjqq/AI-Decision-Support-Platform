package com.aidsp.platform.agent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Agent 模块健康检查端点。
 */
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "agent", "status", "UP");
    }
}
