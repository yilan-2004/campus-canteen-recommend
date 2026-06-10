CREATE DATABASE IF NOT EXISTS shitang_recommend
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE shitang_recommend;

DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS user_behavior;
DROP TABLE IF EXISTS dish_tag;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS dish;
DROP TABLE IF EXISTS dish_category;
DROP TABLE IF EXISTS canteen_window;
DROP TABLE IF EXISTS canteen;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  username VARCHAR(64) NOT NULL COMMENT '用户名',
  password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
  role VARCHAR(32) NOT NULL COMMENT '角色：STUDENT/MERCHANT/ADMIN',
  student_no VARCHAR(32) NULL COMMENT '学号，学生角色使用',
  real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
  college VARCHAR(128) NULL COMMENT '学院',
  grade VARCHAR(32) NULL COMMENT '年级',
  taste_preference VARCHAR(255) NULL COMMENT '口味偏好，逗号分隔',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username),
  UNIQUE KEY uk_user_student_no (student_no),
  KEY idx_user_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE canteen (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  name VARCHAR(100) NOT NULL COMMENT '食堂名称',
  location VARCHAR(255) NOT NULL COMMENT '位置',
  open_time VARCHAR(100) NULL COMMENT '营业时间',
  description VARCHAR(500) NULL COMMENT '简介',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_canteen_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食堂表';

CREATE TABLE canteen_window (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  canteen_id BIGINT UNSIGNED NOT NULL COMMENT '所属食堂ID',
  merchant_id BIGINT UNSIGNED NULL COMMENT '绑定商户用户ID',
  name VARCHAR(100) NOT NULL COMMENT '窗口名称',
  floor VARCHAR(32) NULL COMMENT '楼层',
  description VARCHAR(500) NULL COMMENT '简介',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_window_canteen_status (canteen_id, status),
  KEY idx_window_merchant (merchant_id),
  CONSTRAINT fk_window_canteen FOREIGN KEY (canteen_id) REFERENCES canteen(id),
  CONSTRAINT fk_window_merchant FOREIGN KEY (merchant_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食堂窗口表';

CREATE TABLE dish_category (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  name VARCHAR(64) NOT NULL COMMENT '分类名称',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_category_name (name),
  KEY idx_category_status_sort (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品分类表';

CREATE TABLE dish (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  window_id BIGINT UNSIGNED NOT NULL COMMENT '所属窗口ID',
  category_id BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  name VARCHAR(100) NOT NULL COMMENT '菜品名称',
  image VARCHAR(500) NULL COMMENT '图片地址',
  price DECIMAL(10,2) NOT NULL COMMENT '价格',
  description VARCHAR(1000) NULL COMMENT '描述',
  taste VARCHAR(100) NULL COMMENT '口味',
  calories INT NULL COMMENT '热量',
  avg_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '平均评分',
  sales_count INT NOT NULL DEFAULT 0 COMMENT '销量',
  view_count INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架，0下架，2待审核',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_dish_window_status (window_id, status),
  KEY idx_dish_category_status (category_id, status),
  KEY idx_dish_price (price),
  KEY idx_dish_hot (status, sales_count, avg_rating),
  KEY idx_dish_name (name),
  CONSTRAINT fk_dish_window FOREIGN KEY (window_id) REFERENCES canteen_window(id),
  CONSTRAINT fk_dish_category FOREIGN KEY (category_id) REFERENCES dish_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品表';

CREATE TABLE tag (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  name VARCHAR(64) NOT NULL COMMENT '标签名称',
  type VARCHAR(32) NOT NULL COMMENT '标签类型：TASTE/NUTRITION/SCENE/OTHER',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tag_name_type (name, type),
  KEY idx_tag_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';

CREATE TABLE dish_tag (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
  tag_id BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dish_tag (dish_id, tag_id),
  KEY idx_dish_tag_tag (tag_id),
  CONSTRAINT fk_dish_tag_dish FOREIGN KEY (dish_id) REFERENCES dish(id),
  CONSTRAINT fk_dish_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品标签关联表';

CREATE TABLE user_behavior (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
  behavior_type VARCHAR(32) NOT NULL COMMENT '行为类型：VIEW/LIKE/FAVORITE/COMMENT/RATE/ORDER',
  behavior_weight DECIMAL(6,2) NOT NULL DEFAULT 1.00 COMMENT '行为权重',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_behavior_user_time (user_id, create_time),
  KEY idx_behavior_dish_type (dish_id, behavior_type),
  KEY idx_behavior_type_time (behavior_type, create_time),
  CONSTRAINT fk_behavior_user FOREIGN KEY (user_id) REFERENCES `user`(id),
  CONSTRAINT fk_behavior_dish FOREIGN KEY (dish_id) REFERENCES dish(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户行为表';

CREATE TABLE favorite (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_favorite_user_dish (user_id, dish_id),
  KEY idx_favorite_dish (dish_id),
  CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES `user`(id),
  CONSTRAINT fk_favorite_dish FOREIGN KEY (dish_id) REFERENCES dish(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';

CREATE TABLE `comment` (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
  content VARCHAR(1000) NOT NULL COMMENT '评论内容',
  rating DECIMAL(3,2) NOT NULL COMMENT '评分',
  reply VARCHAR(1000) NULL COMMENT '商户回复',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1显示，0隐藏',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_comment_dish_status (dish_id, status),
  KEY idx_comment_user_time (user_id, create_time),
  CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES `user`(id),
  CONSTRAINT fk_comment_dish FOREIGN KEY (dish_id) REFERENCES dish(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

CREATE TABLE orders (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  status VARCHAR(32) NOT NULL DEFAULT 'PAID' COMMENT '订单状态：CREATED/PAID/CANCELLED',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_orders_user_time (user_id, create_time),
  KEY idx_orders_status_time (status, create_time),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';

CREATE TABLE order_item (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  order_id BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  dish_id BIGINT UNSIGNED NOT NULL COMMENT '菜品ID',
  quantity INT NOT NULL COMMENT '数量',
  price DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
  PRIMARY KEY (id),
  KEY idx_order_item_order (order_id),
  KEY idx_order_item_dish (dish_id),
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
  CONSTRAINT fk_order_item_dish FOREIGN KEY (dish_id) REFERENCES dish(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单详情表';
