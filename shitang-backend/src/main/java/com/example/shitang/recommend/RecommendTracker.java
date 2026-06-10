package com.example.shitang.recommend;

import com.example.shitang.entity.RecommendExposureLog;
import com.example.shitang.mapper.RecommendExposureLogMapper;
import com.example.shitang.vo.RecommendDishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 推荐埋点服务
 *
 * - IMPRESSION:批量写入一次推荐列表的曝光
 * - CLICK / ORDER:单条写入
 *
 * 写入采用 @Async 异步,不阻塞推荐响应
 * 关键字段(traceId/userId/variant)由调用方显式传入,避免 ThreadLocal 在异步线程失效
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendTracker {

    private final RecommendExposureLogMapper mapper;

    @Async("recommendTrackerExecutor")
    public void trackImpression(String scenario, List<RecommendDishVO> items, String requestUri,
                                String traceId, Long userId, String variant) {
        if (items == null || items.isEmpty()) return;
        try {
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < items.size(); i++) {
                RecommendDishVO vo = items.get(i);
                RecommendExposureLog log = new RecommendExposureLog();
                log.setTraceId(traceId);
                log.setUserId(userId);
                log.setScenario(scenario);
                log.setAbVariant(variant);
                log.setDishId(vo.getId());
                log.setRankPosition(i + 1);
                log.setScore(vo.getScore());
                log.setAction("IMPRESSION");
                log.setRequestUri(requestUri);
                log.setCreateTime(now);
                mapper.insert(log);
            }
        } catch (Exception e) {
            log.warn("记录推荐曝光失败", e);
        }
    }

    @Async("recommendTrackerExecutor")
    public void trackAction(String scenario, Long dishId, String action,
                            String traceId, Long userId, String variant) {
        if (dishId == null) return;
        try {
            RecommendExposureLog log = new RecommendExposureLog();
            log.setTraceId(traceId);
            log.setUserId(userId);
            log.setScenario(scenario);
            log.setAbVariant(variant);
            log.setDishId(dishId);
            log.setRankPosition(0);
            log.setAction(action);
            log.setRequestUri(null);
            log.setCreateTime(LocalDateTime.now());
            mapper.insert(log);
        } catch (Exception e) {
            log.warn("记录推荐反馈失败", e);
        }
    }
}
