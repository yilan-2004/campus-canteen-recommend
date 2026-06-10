package com.example.shitang.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 推荐效果评估报告
 */
@Data
@Schema(description = "推荐效果评估报告")
public class EvaluationReport {
    @Schema(description = "评估时间窗(天)")
    private Integer windowDays;
    @Schema(description = "评估的策略/场景,如 PERSONALIZED/MIXED/HOT 等")
    private String scenario;
    @Schema(description = "A 变体指标")
    private Metrics variantA;
    @Schema(description = "B 变体指标(B 未启用时为 null)")
    private Metrics variantB;
    @Schema(description = "建议(基于指标的对比)")
    private String advice;
    @Schema(description = "每种策略的分场景小计")
    private Map<String, Metrics> perScenario;

    @Data
    public static class Metrics {
        @Schema(description = "曝光数")
        private long impressions;
        @Schema(description = "点击数")
        private long clicks;
        @Schema(description = "下单数")
        private long orders;
        @Schema(description = "点击率 CTR")
        private BigDecimal ctr;
        @Schema(description = "转化率 CVR")
        private BigDecimal cvr;
        @Schema(description = "推荐菜品数(去重)")
        private long uniqueDishes;
        @Schema(description = "覆盖用户数")
        private long uniqueUsers;
    }
}
