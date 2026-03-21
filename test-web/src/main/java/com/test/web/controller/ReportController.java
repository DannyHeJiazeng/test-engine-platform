package com.test.web.controller;

import com.test.web.dto.ApiResponse;
import com.test.web.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 报告控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "Report", description = "报告相关接口")
public class ReportController {

    private final ReportService reportService;

    /**
     * 获取测试报告
     */
    @GetMapping("/{executionId}")
    @Operation(summary = "获取测试报告")
    public ApiResponse<ReportService.TestReport> getReport(@PathVariable String executionId) {
        ReportService.TestReport report = reportService.generateReport(executionId);
        return ApiResponse.success(report);
    }

    /**
     * 获取 HTML 报告
     */
    @GetMapping(value = "/{executionId}/html", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "获取 HTML 报告")
    public String getHtmlReport(@PathVariable String executionId) {
        return reportService.generateHtmlReport(executionId);
    }

    /**
     * 获取汇总报告
     */
    @GetMapping("/summary")
    @Operation(summary = "获取汇总报告")
    public ApiResponse<ReportService.SummaryReport> getSummaryReport(
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {

        if (start == null) start = LocalDateTime.now().minusDays(7);
        if (end == null) end = LocalDateTime.now();

        ReportService.SummaryReport report = reportService.generateSummaryReport(start, end);
        return ApiResponse.success(report);
    }

    /**
     * 获取趋势报告
     */
    @GetMapping("/trend")
    @Operation(summary = "获取趋势报告")
    public ApiResponse<ReportService.TrendReport> getTrendReport(
            @RequestParam(defaultValue = "7") int days) {

        ReportService.TrendReport report = reportService.generateTrendReport(days);
        return ApiResponse.success(report);
    }
}