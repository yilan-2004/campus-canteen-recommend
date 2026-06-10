USE shitang_recommend;

-- 初始化账号明文密码均为：123456
-- 下方 password 字段为 BCrypt 哈希值，系统不会存储明文密码。
SET @pwd = '$2b$12$niN67qXABZrjup7i5UrTB.e.87TeU0mhxTloGJnwgLU/OgPBby2KC';

INSERT INTO `user` (id, username, password, role, student_no, real_name, college, grade, taste_preference, status) VALUES
(1, 'admin', @pwd, 'ADMIN', NULL, '系统管理员', '信息化中心', NULL, NULL, 1),
(2, 'merchant1', @pwd, 'MERCHANT', NULL, '张师傅', '后勤集团', NULL, '川湘,微辣', 1),
(3, 'merchant2', @pwd, 'MERCHANT', NULL, '李师傅', '后勤集团', NULL, '清淡,轻食', 1),
(4, 'student1', @pwd, 'STUDENT', '20230001', '李明', '计算机学院', '2023级', '微辣,米饭,高蛋白', 1),
(5, 'student2', @pwd, 'STUDENT', '20230002', '王芳', '软件学院', '2022级', '清淡,低脂,面食', 1);

INSERT INTO canteen (id, name, location, open_time, description, status) VALUES
(1, '第一食堂', '校园东区生活广场', '06:30-20:30', '主打大众快餐、川湘菜和特色套餐，距离教学楼较近。', 1),
(2, '第二食堂', '校园西区宿舍区', '07:00-21:00', '提供轻食、面食、饮品和夜宵，适合宿舍区学生就近用餐。', 1);

INSERT INTO canteen_window (id, canteen_id, merchant_id, name, floor, description, status) VALUES
(1, 1, 2, '川湘小炒窗口', '一楼', '提供微辣、中辣川湘风味小炒和盖饭。', 1),
(2, 1, 2, '经典套餐窗口', '一楼', '提供米饭套餐、家常菜和经济套餐。', 1),
(3, 2, 3, '轻食沙拉窗口', '二楼', '提供低脂、高蛋白轻食和健康套餐。', 1),
(4, 2, 3, '面食粉面窗口', '一楼', '提供牛肉面、拌面、米粉和馄饨。', 1);

INSERT INTO dish_category (id, name, sort, status) VALUES
(1, '米饭套餐', 1, 1),
(2, '面食', 2, 1),
(3, '轻食', 3, 1),
(4, '饮品', 4, 1),
(5, '小吃', 5, 1);

INSERT INTO tag (id, name, type) VALUES
(1, '微辣', 'TASTE'),
(2, '清淡', 'TASTE'),
(3, '高蛋白', 'NUTRITION'),
(4, '低脂', 'NUTRITION'),
(5, '热销', 'SCENE'),
(6, '学生最爱', 'SCENE'),
(7, '米饭类', 'SCENE'),
(8, '面食类', 'SCENE'),
(9, '素食', 'NUTRITION'),
(10, '酸甜', 'TASTE');

INSERT INTO dish (id, window_id, category_id, name, image, price, description, taste, calories, avg_rating, sales_count, view_count, favorite_count, like_count, status) VALUES
(1, 1, 1, '宫保鸡丁盖饭', 'https://images.unsplash.com/photo-1603133872878-684f208fb84b', 15.80, '鸡丁鲜嫩，花生香脆，酸甜微辣，适合作为午餐主食。', '微辣', 680, 4.70, 168, 520, 78, 96, 1),
(2, 1, 1, '鱼香肉丝盖饭', 'https://images.unsplash.com/photo-1563379091339-03246963d7d6', 14.80, '经典川味下饭菜，酸甜咸鲜，搭配米饭。', '微辣', 720, 4.60, 142, 460, 66, 82, 1),
(3, 1, 5, '香辣鸡腿', 'https://images.unsplash.com/photo-1562967914-608f82629710', 9.90, '外皮焦香，肉质鲜嫩，适合作为加餐小吃。', '中辣', 430, 4.50, 120, 330, 41, 70, 1),
(4, 2, 1, '番茄炒蛋套餐', 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38', 12.80, '经典家常口味，酸甜开胃，适合清淡饮食。', '酸甜', 610, 4.40, 108, 300, 38, 55, 1),
(5, 2, 1, '黑椒牛柳饭', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c', 18.80, '牛柳嫩滑，黑椒浓郁，蛋白质丰富。', '咸鲜', 760, 4.80, 132, 390, 60, 88, 1),
(6, 3, 3, '鸡胸肉能量碗', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd', 19.90, '鸡胸肉、玉米、蔬菜和糙米组合，适合健身人群。', '清淡', 520, 4.90, 96, 410, 72, 91, 1),
(7, 3, 3, '低脂蔬菜沙拉', 'https://images.unsplash.com/photo-1540420773420-3366772f4999', 13.90, '多种新鲜蔬菜搭配低脂酱汁，轻负担。', '清淡', 260, 4.30, 75, 280, 35, 44, 1),
(8, 4, 2, '红烧牛肉面', 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624', 16.80, '牛肉汤底浓郁，面条劲道，校园热门面食。', '咸鲜', 690, 4.70, 180, 570, 80, 110, 1),
(9, 4, 2, '葱油拌面', 'https://images.unsplash.com/photo-1612929633738-8fe44f7ec841', 10.80, '葱香浓郁，价格实惠，出餐速度快。', '清淡', 550, 4.20, 89, 240, 25, 39, 1),
(10, 3, 4, '鲜榨橙汁', 'https://images.unsplash.com/photo-1600271886742-f049cd451bba', 8.80, '新鲜橙子榨取，酸甜清爽。', '酸甜', 150, 4.60, 101, 260, 42, 58, 1);

INSERT INTO dish_tag (dish_id, tag_id) VALUES
(1, 1), (1, 3), (1, 5), (1, 6), (1, 7),
(2, 1), (2, 5), (2, 7),
(3, 1), (3, 5), (3, 6),
(4, 2), (4, 7), (4, 10),
(5, 3), (5, 5), (5, 7),
(6, 2), (6, 3), (6, 4),
(7, 2), (7, 4), (7, 9),
(8, 3), (8, 5), (8, 6), (8, 8),
(9, 2), (9, 8),
(10, 2), (10, 10);

INSERT INTO favorite (user_id, dish_id) VALUES
(4, 1), (4, 5), (4, 8),
(5, 6), (5, 7), (5, 9);

INSERT INTO `comment` (user_id, dish_id, content, rating, reply, status) VALUES
(4, 1, '宫保鸡丁很下饭，微辣刚好，推荐。', 4.80, '感谢支持，欢迎下次再来。', 1),
(4, 8, '牛肉面汤底很香，分量也足。', 4.70, NULL, 1),
(5, 6, '鸡胸肉能量碗比较清爽，适合晚餐。', 4.90, '谢谢评价，我们会继续优化搭配。', 1),
(5, 7, '沙拉新鲜，但希望酱汁可以多一种选择。', 4.20, NULL, 1);

INSERT INTO orders (id, user_id, total_amount, status, create_time) VALUES
(1, 4, 32.60, 'PAID', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 4, 16.80, 'PAID', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 5, 33.80, 'PAID', DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO order_item (order_id, dish_id, quantity, price) VALUES
(1, 1, 1, 15.80),
(1, 8, 1, 16.80),
(2, 8, 1, 16.80),
(3, 6, 1, 19.90),
(3, 7, 1, 13.90);

INSERT INTO user_behavior (user_id, dish_id, behavior_type, behavior_weight, create_time) VALUES
(4, 1, 'VIEW', 1.00, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(4, 1, 'FAVORITE', 3.00, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 1, 'RATE', 4.80, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 1, 'ORDER', 5.00, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 5, 'VIEW', 1.00, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 5, 'FAVORITE', 3.00, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 8, 'VIEW', 1.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 8, 'LIKE', 2.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 8, 'ORDER', 5.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 6, 'VIEW', 1.00, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 6, 'FAVORITE', 3.00, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 6, 'ORDER', 5.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 7, 'VIEW', 1.00, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 7, 'FAVORITE', 3.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 9, 'VIEW', 1.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 9, 'LIKE', 2.00, DATE_SUB(NOW(), INTERVAL 2 DAY));
