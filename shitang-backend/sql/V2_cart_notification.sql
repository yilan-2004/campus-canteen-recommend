-- 购物车表
CREATE TABLE IF NOT EXISTS `user_cart` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_dish` (`user_id`, `dish_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户购物车';

-- 消息通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `type` VARCHAR(32) NOT NULL COMMENT '类型:ORDER_STATUS/COMMENT_REPLY/SYSTEM',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `content` VARCHAR(512) DEFAULT '' COMMENT '内容',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联ID(订单ID/评论ID)',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读 0未读 1已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';
