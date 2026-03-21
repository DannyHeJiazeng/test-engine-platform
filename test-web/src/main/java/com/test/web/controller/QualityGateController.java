package com.test.web.controller;

import com.test.middleware.QualityGate;
import com.test.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质量门禁控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/quality-gate")
@RequiredArgsConstructor
@Tag(name = "Quality Gate", description = "质量门禁相关接口")
public class QualityGateController {

    private final QualityGate qualityGate;

    /**
     * 获取质量门禁状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取质量门禁状态")
    public ApiResponse<QualityGate.QualityGateResult> getStatus(@RequestParam(required = false) String buildId) {
        if (buildId != null) {
            QualityGate.QualityGateResult result = qualityGate.getHistory(buildId);
            if (result == null) {
                return ApiResponse.error("Build not found: " + buildId);
            }
            return ApiResponse.success(result);
        }
        return ApiResponse.success(null);
    }

    /**
     * 获取质量门禁历史
     */
    @GetMapping("/history")
    @Operation(summary = "获取质量门禁历史")
    public ApiResponse<List<QualityGate.QualityGateHistory>> getHistory() {
        return ApiResponse.success(qualityGate.getHistory());
    }

    /**
     * 清除历史记录
     */
    @DeleteMapping("/history")
    @Operation(summary = "清除质量门禁历史")
    public ApiResponse<Void> clearHistory() {
        qualityGate.clearAllHistory();
        return ApiResponse.success(null, "History cleared");
    }

    /**
     * 获取默认阈值配置
     */
    @GetMapping("/threshold/default")
    @Operation(summary = "获取默认阈值配置")
    public ApiResponse<QualityGate.QualityThreshold> getDefaultThreshold() {
        return ApiResponse.success(QualityGate.QualityThreshold.defaultThreshold());
    }

    /**
     * 获取宽松阈值配置
     */
    @GetMapping("/threshold/relaxed")
    @Operation(summary = "获取宽松阈值配置")
    public ApiResponse<QualityGate.QualityThreshold> getRelaxedThreshold() {
        return ApiResponse.success(QualityGate.QualityThreshold.relaxedThreshold());
    }

    /**
     * 获取严格阈值配置
     */
    @GetMapping("/threshold/strict")
    @Operation(summary = "获取严格阈值配置")
    public ApiResponse<QualityGate.QualityThreshold> getStrictThreshold() {
        return ApiResponse.success(QualityGate.QualityThreshold.strictThreshold());
    }
}