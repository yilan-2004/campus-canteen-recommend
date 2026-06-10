package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.recommend.RecommendContext;
import com.example.shitang.recommend.RecommendTracker;
import com.example.shitang.service.RecommendationService;
import com.example.shitang.vo.RecommendDishVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 第五阶段:推荐接口(集成缓存 + A/B + 埋点)
 *
 * 接口:
 *  GET /api/recommend/hot?limit=10            热门
 *  GET /api/recommend/high-score?limit=10     高分
 *  GET /api/recommend/user/{userId}?limit=10  个性化
 *  GET /api/recommend/mixed?limit=10          混合
 *  GET /api/recommend/scenarios?scenario=...  场景
 *  POST /api/recommend/feedback               埋点反馈(点击/转化)
 */
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendationService recommendationService;
    private final RecommendTracker tracker;

    @GetMapping("/hot")
    public Result<List<RecommendDishVO>> hot(@RequestParam(defaultValue = "10") int limit, HttpServletRequest request) {
        Long userId = null;
        String variant = "A";
        initContext(userId, variant);
        List<RecommendDishVO> data = recommendationService.hot(clamp(limit, 1, 50));
        tracker.trackImpression("HOT", data, request.getRequestURI(),
                RecommendContext.currentTraceId(), userId, variant);
        return Result.success(data);
    }

    @GetMapping("/high-score")
    public Result<List<RecommendDishVO>> highScore(@RequestParam(defaultValue = "10") int limit, HttpServletRequest request) {
        initContext(null, "A");
        List<RecommendDishVO> data = recommendationService.highScore(clamp(limit, 1, 50));
        tracker.trackImpression("HIGH_SCORE", data, request.getRequestURI(),
                RecommendContext.currentTraceId(), null, "A");
        return Result.success(data);
    }

    @GetMapping("/user/{userId}")
    public Result<List<RecommendDishVO>> forUser(@PathVariable Long userId,
                                                  @RequestParam(defaultValue = "10") int limit,
                                                  HttpServletRequest request) {
        String variant = initContextWithVariant(userId);
        List<RecommendDishVO> data = recommendationService.forUser(userId, clamp(limit, 1, 50));
        tracker.trackImpression("PERSONALIZED", data, request.getRequestURI(),
                RecommendContext.currentTraceId(), userId, variant);
        return Result.success(data);
    }

    @GetMapping("/mixed")
    public Result<List<RecommendDishVO>> mixed(@RequestParam Long userId,
                                               @RequestParam(defaultValue = "10") int limit,
                                               HttpServletRequest request) {
        String variant = initContextWithVariant(userId);
        List<RecommendDishVO> data = recommendationService.mixed(userId, clamp(limit, 1, 50));
        tracker.trackImpression("MIXED", data, request.getRequestURI(),
                RecommendContext.currentTraceId(), userId, variant);
        return Result.success(data);
    }

    @GetMapping("/scenarios")
    public Result<List<RecommendDishVO>> scenarios(@RequestParam String scenario,
                                                   @RequestParam(required = false) Long userId,
                                                   @RequestParam(defaultValue = "10") int limit,
                                                   HttpServletRequest request) {
        initContext(userId, "A");
        List<RecommendDishVO> data = recommendationService.byScenario(scenario, userId, clamp(limit, 1, 50));
        tracker.trackImpression("SCENE:" + scenario.toUpperCase(), data, request.getRequestURI(),
                RecommendContext.currentTraceId(), userId, "A");
        return Result.success(data);
    }

    /**
     * 前端在用户点击推荐卡片 / 通过推荐下单时调用
     * 示例:POST /api/recommend/feedback?scenario=PERSONALIZED&dishId=8&action=CLICK
     */
    @PostMapping("/feedback")
    public Result<Void> feedback(@RequestParam String scenario,
                                 @RequestParam Long dishId,
                                 @RequestParam String action) {
        initContext(null, "A");
        if (!"CLICK".equals(action) && !"ORDER".equals(action)) {
            return Result.fail(400, "action 仅支持 CLICK/ORDER");
        }
        tracker.trackAction(scenario, dishId, action,
                RecommendContext.currentTraceId(), null, "A");
        return Result.success();
    }

    private int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private void initContext(Long userId, String variant) {
        String traceId = RecommendContext.currentTraceId();
        if (traceId == null || traceId.equals("-")) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        RecommendContext.set(traceId, userId, variant);
    }

    private String initContextWithVariant(Long userId) {
        String traceId = RecommendContext.currentTraceId();
        if (traceId == null || traceId.equals("-")) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        String variant = "A";
        if (userId != null) {
            int hash = Math.abs(String.valueOf(userId).hashCode()) % 100;
            variant = hash < 50 ? "A" : "B";
        }
        RecommendContext.set(traceId, userId, variant);
        return variant;
    }
}
