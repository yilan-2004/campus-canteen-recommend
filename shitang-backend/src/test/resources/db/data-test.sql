-- Smoke test 用的精简测试数据
-- (BCrypt hash for '123456' is the standard one used across the project)
INSERT INTO "user" (id, username, password, role, real_name, taste_preference, status)
VALUES
(1, 'admin', '$2b$12$niN67qXABZrjup7i5UrTB.e.87TeU0mhxTloGJnwgLU/OgPBby2KC', 'ADMIN', '管理员', NULL, 1),
(2, 'merchant1', '$2b$12$niN67qXABZrjup7i5UrTB.e.87TeU0mhxTloGJnwgLU/OgPBby2KC', 'MERCHANT', '张师傅', '川湘,微辣', 1),
(4, 'student1', '$2b$12$niN67qXABZrjup7i5UrTB.e.87TeU0mhxTloGJnwgLU/OgPBby2KC', 'STUDENT', '李明', '微辣,米饭,高蛋白', 1);

INSERT INTO canteen (id, name, location, status) VALUES (1, '一食堂', '东区', 1);
INSERT INTO canteen_window (id, canteen_id, name, status) VALUES (1, 1, '川湘窗口', 1);
INSERT INTO dish_category (id, name, sort, status) VALUES (1, '米饭套餐', 1, 1);

INSERT INTO tag (id, name, type) VALUES
(1, '微辣', 'TASTE'),
(2, '清淡', 'TASTE'),
(3, '高蛋白', 'NUTRITION'),
(4, '低脂', 'NUTRITION'),
(5, '热销', 'SCENE'),
(6, '学生最爱', 'SCENE'),
(7, '米饭类', 'SCENE'),
(8, '面食类', 'SCENE');

INSERT INTO dish (id, window_id, category_id, name, price, taste, calories, avg_rating, sales_count, view_count, favorite_count, like_count, status) VALUES
(1, 1, 1, '宫保鸡丁盖饭', 15.80, '微辣', 680, 4.70, 168, 520, 78, 96, 1),
(2, 1, 1, '鱼香肉丝盖饭', 14.80, '微辣', 720, 4.60, 142, 460, 66, 82, 1),
(3, 1, 1, '黑椒牛柳饭', 18.80, '咸鲜', 760, 4.80, 132, 390, 60, 88, 1),
(4, 1, 1, '鸡胸肉能量碗', 19.90, '清淡', 520, 4.90, 96, 410, 72, 91, 1);

INSERT INTO dish_tag (dish_id, tag_id) VALUES
(1, 1), (1, 3), (1, 5), (1, 6), (1, 7),
(2, 1), (2, 5), (2, 7),
(3, 3), (3, 5), (3, 7),
(4, 2), (4, 3), (4, 4);

INSERT INTO favorite (user_id, dish_id) VALUES (4, 1), (4, 3);

INSERT INTO comment (user_id, dish_id, content, rating, status) VALUES
(4, 1, '下饭', 4.80, 1),
(4, 3, '好吃', 4.70, 1);

INSERT INTO user_behavior (user_id, dish_id, behavior_type, behavior_weight) VALUES
(4, 1, 'VIEW', 1.00),
(4, 1, 'FAVORITE', 3.00),
(4, 1, 'ORDER', 5.00),
(4, 3, 'VIEW', 1.00),
(4, 3, 'ORDER', 5.00);
