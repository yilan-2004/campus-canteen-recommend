package com.example.shitang.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 推荐系统配置项
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "recommend")
public class RecommendProperties {

    private Cache cache = new Cache();
    private AbTest abTest = new AbTest();

    @Data
    public static class Cache {
        private boolean enabled = true;
        private long hotTtlSeconds = 300;
        private long highScoreTtlSeconds = 600;
        private long scenarioTtlSeconds = 600;
        private long personalizedTtlSeconds = 180;
    }

    @Data
    public static class AbTest {
        private boolean enabled = false;
        private int strategyAWeight = 50;
        private int strategyBWeight = 50;
    }
}
