package com.aidsp.platform.report.controller;

import com.aidsp.platform.core.api.PageResponse;
import com.aidsp.platform.report.api.ReportGenerateRequest;
import com.aidsp.platform.report.api.ReportHistoryItemVO;
import com.aidsp.platform.report.api.ReportVO;
import com.aidsp.platform.report.service.ReportServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 报告模块 REST 控制器。
 * <p>所有方法由 {@code GlobalRestResponseAdvice} 自动包装为 {@code RestResponse<T>}。
 * <p>端点：
 * <ul>
 *   <li>GET  /api/v1/reports/health</li>
 *   <li>GET  /api/v1/reports                  报告列表（分页）</li>
 *   <li>GET  /api/v1/reports/{id}             报告详情</li>
 *   <li>POST /api/v1/reports/generate         生成报告（同步调用 Agent）</li>
 *   <li>DELETE /api/v1/reports/{id}           删除报告</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportServiceImpl reportService;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("module", "report", "status", "UP");
    }

    @GetMapping
    public PageResponse<ReportHistoryItemVO> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false, defaultValue = "1") Long page,
            @RequestParam(required = false, defaultValue = "20") Long size) {
        return reportService.page(keyword, companyId, page, size);
    }

    @GetMapping("/{id}")
    public ReportVO getById(@PathVariable("id") String id) {
        return reportService.getById(id);
    }

    /**
     * 同步生成报告：调 Agent 后阻塞 1.5-2.5s 返回完整报告 VO。
     * <p>前端拿到 VO 后直接 navigate 到 {@code /reports/{id}} 即可。
     */
    @PostMapping("/generate")
    public ReportVO generate(@Valid @RequestBody ReportGenerateRequest request) {
        log.info("[ReportController] generate report, companyId={}, companyAnalysisId={}",
                request == null ? null : request.getCompanyId(),
                request == null ? null : request.getCompanyAnalysisId());
        return reportService.generate(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        reportService.deleteById(id);
    }
}
