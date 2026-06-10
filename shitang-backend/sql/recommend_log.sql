-- 第五阶段:推荐系统埋点表
-- 记录每一次推荐曝光/点击/转化,用于 A/B 评估
USE shitang_recommend;
DROP TABLE IF EXISTS recommend_exposure_log;
CREATE TABLE recommend_exposure_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  trace_id VARCHAR(64) NOT NULL COMMENT '请求追踪ID',
  user_id BIGINT UNSIGNED NULL COMMENT '用户ID(匿名推荐时为 NULL)',
  scenario VARCHAR(32) NOT NULL COMMENT '推荐场景:HOT/HIGH_SCORE/CONTENT/CF/MIXED/SCENE',
  ab_variant VARCHAR(8) NOT NULL DEFAULT 'A' COMMENT 'A/B 变体:A/B',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
  rank_position INT NOT NULL COMMENT '推荐位次(1-based)',
  score DECIMAL(8,4) NULL COMMENT '推荐分',
  action VARCHAR(16) NOT NULL COMMENT '用户动作:IMPRESSION/CLICK/ORDER',
  request_uri VARCHAR(255) NULL COMMENT '触发接口',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (id),
  KEY idx_expo_user_time (user_id, create_time),
  KEY idx_expo_scenario_variant (scenario, ab_variant, create_time),
  KEY idx_expo_dish (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐埋点表';
