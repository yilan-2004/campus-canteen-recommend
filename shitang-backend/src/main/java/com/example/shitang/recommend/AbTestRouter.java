package com.example.shitang.recommend;

import com.example.shitang.config.RecommendProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * A/B 测试分流器
 *
 * 基于 userId 哈希值映射到策略变体,保证同一用户始终落到同一桶(粘性)
 *
 * 策略变体:
 *   A: 基础混合 (0.5*个性化 + 0.3*热门 + 0.2*高分)
 *   B: 强内容 (0.7*内容 + 0.2*热门 + 0.1*高分,弱协同)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AbTestRouter {

    private final RecommendProperties properties;

    public String route(Long userId) {
        if (!properties.getAbTest().isEnabled()) {
            return "A";
        }
        if (userId == null) {
            return "A";
        }
        int hash = Math.abs(String.valueOf(userId).hashCode()) % 100;
        int aWeight = properties.getAbTest().getStrategyAWeight();
        return hash < aWeight ? "A" : "B";
    }

    public boolean isEnabled() {
        return properties.getAbTest().isEnabled();
    }
}
