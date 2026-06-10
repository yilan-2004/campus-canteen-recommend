package com.example.shitang;

import com.example.shitang.vo.RecommendDishVO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 第五阶段:Spring Boot 集成测试
 *  - 启动完整应用上下文
 *  - 验证 H2 + 内存数据下推荐接口能跑通
 */
@SpringBootTest
@ActiveProfiles("smoke")
class RecommendIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private com.example.shitang.service.RecommendationService recommendationService;

    @BeforeAll
    static void note() {}

    @org.junit.jupiter.api.Test
    void setupSchema() throws Exception {
        // 测试启动时若 schema 为空,主动执行 schema/data 脚本
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM dish")) {
            rs.next();
            int count = rs.getInt(1);
            if (count == 0) {
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/schema-test.sql"));
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/data-test.sql"));
            }
        } catch (Exception e) {
            // 第一次启动时表不存在,执行初始化
            try (var conn = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/schema-test.sql"));
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("db/data-test.sql"));
            }
        }
    }

    @Test
    void hotShouldReturnNonEmpty() {
        List<RecommendDishVO> hot = recommendationService.hot(3);
        assertNotNull(hot);
        assertFalse(hot.isEmpty(), "热门推荐应至少返回 1 个菜品");
        if (hot.size() > 1) {
            assertTrue(hot.get(0).getScore().compareTo(hot.get(1).getScore()) >= 0,
                    "热门应该按 score 降序");
        }
        assertNotNull(hot.get(0).getReason());
        assertNotNull(hot.get(0).getReasonType());
    }

    @Test
    void highScoreShouldReturnTop() {
        List<RecommendDishVO> top = recommendationService.highScore(5);
        assertNotNull(top);
        assertFalse(top.isEmpty());
    }

    @Test
    void forUserShouldUsePreference() {
        List<RecommendDishVO> recos = recommendationService.forUser(4L, 3);
        assertNotNull(recos);
        assertFalse(recos.isEmpty());
    }

    @Test
    void scenarioShouldFilter() {
        List<RecommendDishVO> lunch = recommendationService.byScenario("LUNCH", null, 5);
        assertNotNull(lunch);
        // LUNCH 场景需要 tag=7(米饭类):有 3 道菜(1/2/3)满足,4 鸡胸肉能量碗没米饭类 tag
        assertTrue(lunch.size() >= 3, "LUNCH 场景应至少返回 3 道米饭类菜品,实际 " + lunch.size());
    }

    @Test
    void mixedShouldCombineAllSources() {
        List<RecommendDishVO> mixed = recommendationService.mixed(4L, 5);
        assertNotNull(mixed);
        assertFalse(mixed.isEmpty());
    }
}

