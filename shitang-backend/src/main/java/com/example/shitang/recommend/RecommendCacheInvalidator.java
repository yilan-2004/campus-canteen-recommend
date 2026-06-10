package com.example.shitang.recommend;

import com.example.shitang.recommend.RecommendCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 监听业务事件(收藏/下单/评论)失效推荐缓存
 * 解耦业务代码与推荐缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendCacheInvalidator {

    private final RecommendCache recommendCache;

    public record UserBehaviorEvent(Long userId, String action) {}

    @Async("recommendTrackerExecutor")
    @EventListener
    public void onUserBehavior(UserBehaviorEvent event) {
        if (event == null || event.userId() == null) return;
        log.info("用户 {} 触发行为 {},失效推荐缓存", event.userId(), event.action());
        recommendCache.invalidateUser(event.userId());
    }
}
