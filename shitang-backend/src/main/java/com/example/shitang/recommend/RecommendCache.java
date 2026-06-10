package com.example.shitang.recommend;

import com.example.shitang.config.RecommendProperties;
import com.example.shitang.vo.RecommendDishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 推荐结果缓存工具
 *
 * Key 设计:
 *   recommend:hot:{limit}
 *   recommend:high:{limit}
 *   recommend:scenario:{scenario}:{userId|none}:{limit}
 *   recommend:user:{userId}:{limit}
 *   recommend:mixed:{userId}:{limit}:{abVariant}
 *
 * 缓存开关通过 recommend.cache.enabled 统一控制,关闭时所有方法直接穿透
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendCache {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RecommendProperties properties;

    public List<RecommendDishVO> getHot(int limit) {
        return get(buildKey("hot", String.valueOf(limit)));
    }

    public void setHot(int limit, List<RecommendDishVO> data) {
        set(buildKey("hot", String.valueOf(limit)), data, properties.getCache().getHotTtlSeconds());
    }

    public List<RecommendDishVO> getHighScore(int limit) {
        return get(buildKey("high", String.valueOf(limit)));
    }

    public void setHighScore(int limit, List<RecommendDishVO> data) {
        set(buildKey("high", String.valueOf(limit)), data, properties.getCache().getHighScoreTtlSeconds());
    }

    public List<RecommendDishVO> getScenario(String scenario, Long userId, int limit) {
        return get(buildKey("scenario", scenario, userId == null ? "none" : String.valueOf(userId), String.valueOf(limit)));
    }

    public void setScenario(String scenario, Long userId, int limit, List<RecommendDishVO> data) {
        set(buildKey("scenario", scenario, userId == null ? "none" : String.valueOf(userId), String.valueOf(limit)),
                data, properties.getCache().getScenarioTtlSeconds());
    }

    public List<RecommendDishVO> getPersonalized(Long userId, int limit, String variant) {
        return get(buildKey("user", String.valueOf(userId), String.valueOf(limit), variant));
    }

    public void setPersonalized(Long userId, int limit, String variant, List<RecommendDishVO> data) {
        set(buildKey("user", String.valueOf(userId), String.valueOf(limit), variant),
                data, properties.getCache().getPersonalizedTtlSeconds());
    }

    public List<RecommendDishVO> getMixed(Long userId, int limit, String variant) {
        return get(buildKey("mixed", String.valueOf(userId), String.valueOf(limit), variant));
    }

    public void setMixed(Long userId, int limit, String variant, List<RecommendDishVO> data) {
        set(buildKey("mixed", String.valueOf(userId), String.valueOf(limit), variant),
                data, properties.getCache().getPersonalizedTtlSeconds());
    }

    /** 主动失效(下单/收藏时调用,让个性化立刻反映新行为) */
    public void invalidateUser(Long userId) {
        if (!enabled()) return;
        try {
            String pattern = "recommend:user:" + userId + ":*";
            redisTemplate.delete(keys(pattern));
        } catch (Exception e) {
            log.warn("清除推荐缓存失败 userId={}", userId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<RecommendDishVO> get(String key) {
        if (!enabled()) return null;
        try {
            Object v = redisTemplate.opsForValue().get(key);
            if (v == null) return null;
            if (v instanceof List<?> list) {
                return (List<RecommendDishVO>) list;
            }
            return null;
        } catch (Exception e) {
            log.warn("推荐缓存读取失败 key={}", key, e);
            return null;
        }
    }

    private void set(String key, List<RecommendDishVO> data, long ttlSeconds) {
        if (!enabled() || data == null) return;
        try {
            redisTemplate.opsForValue().set(key, data, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("推荐缓存写入失败 key={}", key, e);
        }
    }

    private boolean enabled() {
        return properties != null && properties.getCache() != null && properties.getCache().isEnabled();
    }

    private String buildKey(String type, String... parts) {
        return "recommend:" + type + ":" + String.join(":", parts);
    }

    private java.util.Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            return java.util.Collections.emptySet();
        }
    }
}
