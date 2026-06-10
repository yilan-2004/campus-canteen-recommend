package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.dto.EvaluationReport;
import com.example.shitang.recommend.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 第五阶段:推荐评估接口
 *  - 按变体统计曝光/点击/转化
 *  - 离线 NDCG 评估
 */
@Tag(name = "推荐评估")
@RestController
@RequestMapping("/api/recommend/eval")
@RequiredArgsConstructor
public class EvalController {

    private final EvaluationService evaluationService;

    @GetMapping("/ab")
    @Operation(summary = "按 A/B 变体汇总埋点指标")
    public Result<EvaluationReport> abReport(@RequestParam(defaultValue = "7") Integer windowDays) {
        return Result.success(evaluationService.reportByAbTest(windowDays));
    }

    @GetMapping("/ndcg")
    @Operation(summary = "离线 NDCG 评估(对最近有 ORDER 行为的用户)")
    public Result<Map<String, Object>> ndcg(@RequestParam(defaultValue = "10") int topN,
                                            @RequestParam(defaultValue = "100") int sampleUsers) {
        return Result.success(evaluationService.offlineNdcg(topN, sampleUsers));
    }
}
