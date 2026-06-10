# 校园食堂个性化推荐系统后端（第二阶段）

项目名称：《基于用户行为分析的校园食堂个性化推荐系统设计与实现》

本目录为第一阶段后端基础工程，已完成 Spring Boot 3 + MyBatis-Plus + MySQL 8 + JWT 登录认证基础结构。

## 1. 当前阶段已实现

- Spring Boot 3 后端 Maven 项目结构
- MyBatis-Plus 基础配置
- MySQL 建表 SQL：`sql/schema.sql`
- 初始化测试数据：`sql/data.sql`
- 统一接口返回：`Result<T>`
- 全局异常处理
- 用户注册、登录、当前用户信息接口
- JWT 鉴权过滤器
- BCrypt 密码加密
- 12 张业务表对应实体类和 Mapper 基础结构

当前阶段只实现后端基础和认证能力，暂不包含前端页面、菜品 CRUD、推荐算法和统计接口。

## 2. 项目目录结构

```text
shitang-backend
├── pom.xml
├── README.md
├── sql
│   ├── schema.sql
│   └── data.sql
└── src/main
    ├── java/com/example/shitang
    │   ├── ShitangBackendApplication.java
    │   ├── common
    │   │   ├── BizException.java
    │   │   ├── GlobalExceptionHandler.java
    │   │   ├── Result.java
    │   │   └── ResultCode.java
    │   ├── config
    │   │   ├── MybatisPlusConfig.java
    │   │   └── SecurityConfig.java
    │   ├── controller
    │   │   ├── AuthController.java
    │   │   └── HealthController.java
    │   ├── dto
    │   │   ├── LoginRequest.java
    │   │   └── RegisterRequest.java
    │   ├── entity
    │   ├── mapper
    │   ├── security
    │   ├── service
    │   ├── utils
    │   └── vo
    └── resources
        └── application.yml
```

## 3. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x

## 4. 数据库初始化

登录 MySQL 后执行：

```bash
mysql -uroot -p < sql/schema.sql
mysql -uroot -p < sql/data.sql
```

或者进入 MySQL 控制台后执行：

```sql
source sql/schema.sql;
source sql/data.sql;
```

默认数据库名：

```text
shitang_recommend
```

## 5. 修改数据库连接

根据本机 MySQL 用户名和密码修改：

```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shitang_recommend?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

## 6. 启动后端

在 `shitang-backend` 目录运行：

```bash
mvn spring-boot:run
```

启动成功后访问：

```text
http://localhost:8080/api/health
```

## 7. 初始化账号

初始化账号明文密码均为：`123456`。

| 角色 | 用户名 | 密码 | 说明 |
|---|---|---|---|
| 管理员 | admin | 123456 | 管理员端测试账号 |
| 商户 | merchant1 | 123456 | 川湘/套餐窗口商户 |
| 商户 | merchant2 | 123456 | 轻食/面食窗口商户 |
| 学生 | student1 | 123456 | 微辣、米饭偏好学生 |
| 学生 | student2 | 123456 | 清淡、低脂偏好学生 |

数据库中保存的是 BCrypt 密文，不保存明文密码。

## 8. 接口测试示例

### 8.1 健康检查

```bash
curl http://localhost:8080/api/health
```

### 8.2 学生注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student3",
    "password": "123456",
    "studentNo": "20260003",
    "realName": "王五",
    "college": "计算机学院",
    "grade": "2023级",
    "tastePreference": "微辣,米饭"
  }'
```

### 8.3 登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"123456"}'
```

返回示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userInfo": {
      "id": 4,
      "username": "student1",
      "role": "STUDENT",
      "studentNo": "20230001",
      "realName": "李明"
    }
  }
}
```

### 8.4 获取当前用户信息

将登录返回的 token 填入请求头：

```bash
curl http://localhost:8080/api/auth/userinfo \
  -H "Authorization: Bearer <登录返回的token>"
```

不携带 token 访问该接口会返回 401。

## 9. 第一阶段接口清单

| 方法 | 路径 | 说明 | 是否需要登录 |
|---|---|---|---|
| GET | `/api/health` | 健康检查 | 否 |
| POST | `/api/auth/register` | 学生注册 | 否 |
| POST | `/api/auth/login` | 用户登录 | 否 |
| GET | `/api/auth/userinfo` | 当前用户信息 | 是 |

## 10. 第二阶段接口清单

第二阶段接口均需要登录后携带 JWT：

```bash
-H "Authorization: Bearer <登录返回的token>"
```

| 模块 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 食堂 | GET | `/api/canteens` | 食堂列表，支持 `keyword`、`status` |
| 食堂 | GET | `/api/canteens/{id}` | 食堂详情 |
| 食堂 | POST | `/api/canteens` | 新增食堂 |
| 食堂 | PUT | `/api/canteens/{id}` | 修改食堂 |
| 食堂 | DELETE | `/api/canteens/{id}` | 停用食堂 |
| 窗口 | GET | `/api/windows` | 窗口列表，支持 `canteenId`、`merchantId`、`keyword`、`status` |
| 窗口 | GET | `/api/windows/{id}` | 窗口详情 |
| 窗口 | POST | `/api/windows` | 新增窗口 |
| 窗口 | PUT | `/api/windows/{id}` | 修改窗口 |
| 窗口 | DELETE | `/api/windows/{id}` | 停用窗口 |
| 分类 | GET | `/api/categories` | 分类列表，支持 `keyword`、`status` |
| 分类 | GET | `/api/categories/{id}` | 分类详情 |
| 分类 | POST | `/api/categories` | 新增分类 |
| 分类 | PUT | `/api/categories/{id}` | 修改分类 |
| 分类 | DELETE | `/api/categories/{id}` | 停用分类 |
| 标签 | GET | `/api/tags` | 标签列表，支持 `keyword`、`type` |
| 标签 | GET | `/api/tags/{id}` | 标签详情 |
| 标签 | POST | `/api/tags` | 新增标签 |
| 标签 | PUT | `/api/tags/{id}` | 修改标签 |
| 标签 | DELETE | `/api/tags/{id}` | 删除未被菜品使用的标签 |
| 菜品 | GET | `/api/dishes` | 菜品列表，支持 `keyword`、`windowId`、`categoryId`、`status`、`minPrice`、`maxPrice` |
| 菜品 | GET | `/api/dishes/{id}` | 菜品详情，包含食堂、窗口、分类、标签信息 |
| 菜品 | POST | `/api/dishes` | 新增菜品 |
| 菜品 | PUT | `/api/dishes/{id}` | 修改菜品 |
| 菜品 | DELETE | `/api/dishes/{id}` | 下架菜品 |
| 菜品 | PUT | `/api/dishes/{id}/status` | 修改菜品状态 |
| 菜品 | PUT | `/api/dishes/{id}/tags` | 设置菜品标签 |

## 11. 第二阶段接口测试示例

### 11.1 登录并保存 token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
```

Windows PowerShell 可以直接复制登录接口返回的 `data.token`，后续替换示例中的 `$TOKEN`。

### 11.2 新增食堂

```bash
curl -X POST http://localhost:8080/api/canteens \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"第三食堂","location":"校园北区","openTime":"07:00-20:00","description":"北区综合食堂"}'
```

### 11.3 查询窗口列表

```bash
curl "http://localhost:8080/api/windows?canteenId=1&status=1" \
  -H "Authorization: Bearer $TOKEN"
```

### 11.4 新增分类

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"早餐","sort":6,"status":1}'
```

### 11.5 新增标签

```bash
curl -X POST http://localhost:8080/api/tags \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"早餐推荐","type":"SCENE"}'
```

### 11.6 新增菜品

```bash
curl -X POST http://localhost:8080/api/dishes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "windowId": 1,
    "categoryId": 1,
    "name": "辣子鸡盖饭",
    "image": "https://images.unsplash.com/photo-1603133872878-684f208fb84b",
    "price": 16.80,
    "description": "香辣下饭，适合喜欢川味的学生",
    "taste": "中辣",
    "calories": 760,
    "status": 1
  }'
```

### 11.7 设置菜品标签

```bash
curl -X PUT http://localhost:8080/api/dishes/1/tags \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"tagIds":[1,3,5,7]}'
```

### 11.8 筛选菜品

```bash
curl "http://localhost:8080/api/dishes?keyword=鸡&categoryId=1&minPrice=10&maxPrice=20&status=1" \
  -H "Authorization: Bearer $TOKEN"
```

### 11.9 菜品上下架

```bash
curl -X PUT "http://localhost:8080/api/dishes/1/status?status=0" \
  -H "Authorization: Bearer $TOKEN"
```

## 12. 第三阶段接口清单

第三阶段新增学生端交互接口，均需要登录后携带 JWT：

```bash
-H "Authorization: Bearer <登录返回的token>"
```

| 模块 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 菜品 | GET | `/api/dishes` | 菜品浏览，支持 `keyword`、`canteenId`、`windowId`、`categoryId`、`tagId`、`taste`、`status`、`minPrice`、`maxPrice` |
| 菜品 | GET | `/api/dishes/{id}` | 菜品详情，并记录 `VIEW` 浏览行为、增加浏览量 |
| 菜品 | POST | `/api/dishes/{id}/like` | 点赞菜品，增加点赞数并记录 `LIKE` 行为 |
| 行为 | POST | `/api/behavior` | 手动记录用户行为 |
| 行为 | GET | `/api/behavior/user/{userId}` | 查看指定用户行为记录 |
| 行为 | GET | `/api/behavior/my` | 查看我的行为记录 |
| 收藏 | POST | `/api/favorites/{dishId}` | 收藏菜品，增加收藏数并记录 `FAVORITE` 行为 |
| 收藏 | DELETE | `/api/favorites/{dishId}` | 取消收藏 |
| 收藏 | GET | `/api/favorites/my` | 我的收藏 |
| 评论 | POST | `/api/comments` | 发表评论和评分，记录 `COMMENT`、`RATE` 行为并重算评分 |
| 评论 | GET | `/api/comments/dish/{dishId}` | 查看菜品评论 |
| 评论 | GET | `/api/comments/my` | 我的评论 |
| 订单 | POST | `/api/orders` | 模拟下单，创建订单明细、增加销量并记录 `ORDER` 行为 |
| 订单 | GET | `/api/orders/my` | 我的订单 |
| 订单 | GET | `/api/orders/admin` | 查看全部订单 |

## 13. 第三阶段接口测试示例

### 13.1 登录学生账号

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"123456"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")
```

### 13.2 浏览菜品列表

```bash
curl "http://localhost:8080/api/dishes?status=1&keyword=鸡&minPrice=10&maxPrice=30" \
  -H "Authorization: Bearer $TOKEN"
```

### 13.3 查看菜品详情并记录浏览行为

```bash
curl http://localhost:8080/api/dishes/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 13.4 点赞菜品

```bash
curl -X POST http://localhost:8080/api/dishes/1/like \
  -H "Authorization: Bearer $TOKEN"
```

### 13.5 收藏和取消收藏

```bash
curl -X POST http://localhost:8080/api/favorites/1 \
  -H "Authorization: Bearer $TOKEN"

curl http://localhost:8080/api/favorites/my \
  -H "Authorization: Bearer $TOKEN"

curl -X DELETE http://localhost:8080/api/favorites/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 13.6 发表评论和评分

```bash
curl -X POST http://localhost:8080/api/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"dishId":1,"content":"味道不错，适合午餐。","rating":4.70}'

curl http://localhost:8080/api/comments/dish/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 13.7 模拟下单

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"items":[{"dishId":1,"quantity":1},{"dishId":8,"quantity":2}]}'

curl http://localhost:8080/api/orders/my \
  -H "Authorization: Bearer $TOKEN"
```

### 13.8 查看我的行为记录

```bash
curl http://localhost:8080/api/behavior/my \
  -H "Authorization: Bearer $TOKEN"
```

## 14. 第四阶段接口清单

第四阶段实现推荐系统，包含热门推荐、高分推荐、个性化推荐（内容过滤 + 协同过滤）、
混合推荐和场景化推荐。所有推荐结果均包含 `score`（推荐分）与 `reason`（推荐原因中文文案）。

### 14.1 通用说明

- 热门/高分/场景三类接口对**未登录用户**开放，匿名访问即可
- 个性化(`/user/{userId}`)和混合(`/mixed`)接口需要登录
- `limit` 参数默认 10，范围 1~50（接口层已 clamp 限幅）
- 推荐原因(reason)会结合用户偏好、历史行为、商品标签动态生成

### 14.2 接口列表

| 模块 | 方法 | 路径 | 说明 | 是否需要登录 |
|---|---|---|---|---|
| 推荐 | GET | `/api/recommend/hot` | 热门推荐，支持 `limit` | 否 |
| 推荐 | GET | `/api/recommend/high-score` | 高分推荐（贝叶斯平均） | 否 |
| 推荐 | GET | `/api/recommend/user/{userId}` | 个性化推荐（内容+协同融合） | 是 |
| 推荐 | GET | `/api/recommend/mixed` | 混合推荐（个性化+热门+评分） | 是 |
| 推荐 | GET | `/api/recommend/scenarios` | 场景化推荐 `scenario=BREAKFAST/LUNCH/DINNER/NIGHT/HEALTHY` | 否 |

### 14.3 算法设计

| 策略 | 公式 / 实现 | 适用场景 |
|---|---|---|
| 热门 | `log10(sales+1)*10 + log10(fav+1)*6 + log10(like+1)*4 + log10(view+1)*2 + avgRating*5` | 冷启动、首页大促位 |
| 高分 | 贝叶斯平均 `(C*m + n*R) / (C+n)`，C=5 为先验 | 高分榜单 |
| 内容 | 命中用户 `taste_preference` + 历史行为反推标签 → 标签/口味权重累加 | 新用户冷启动兜底 |
| 协同 | 用户-菜品行为向量 → 余弦相似度 → 找 Top-5 相似用户 → 聚合其高分菜品 | 有一定行为量的用户 |
| 混合 | `0.5*个性化 + 0.3*热门 + 0.2*高分` | 默认推荐流 |
| 场景 | BREAKFAST/LUNCH/DINNER/NIGHT/HEALTHY 走不同价格/热量/标签过滤 | 首页分时段卡片 |

### 14.4 推荐原因生成规则

- 命中用户偏好 → "符合你的「微辣 / 米饭」偏好"
- 命中历史行为反推标签 → "根据你浏览/收藏过的菜品推荐"
- 协同命中 → "和你口味相似的同学都在点"
- 销量/评分高 → "全校热销 168 份 · 评分 4.70 口碑稳定"
- 场景匹配 → "午餐场景推荐 · 米饭套餐"

### 14.5 测试示例

```bash
# 1. 匿名访问热门
curl "http://localhost:8080/api/recommend/hot?limit=5"

# 2. 匿名访问高分
curl "http://localhost:8080/api/recommend/high-score?limit=5"

# 3. 场景化(午餐)
curl "http://localhost:8080/api/recommend/scenarios?scenario=LUNCH&limit=5"

# 4. 登录后个性化
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"123456"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

curl "http://localhost:8080/api/recommend/user/4?limit=5" \
  -H "Authorization: Bearer $TOKEN"

# 5. 混合推荐
curl "http://localhost:8080/api/recommend/mixed?userId=4&limit=5" \
  -H "Authorization: Bearer $TOKEN"
```

返回示例（已简化）：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 8,
      "name": "红烧牛肉面",
      "avgRating": 4.70,
      "salesCount": 180,
      "tags": [{"name": "高蛋白"}, {"name": "热销"}, {"name": "学生最爱"}, {"name": "面食类"}],
      "score": 7.8521,
      "reasonType": "CONTENT",
      "reason": "符合你的「微辣 / 高蛋白」偏好"
    }
  ]
}
```

## 15. 第五阶段 - 后端深度

第五阶段在第四阶段推荐系统之上,补齐工程化能力:**Redis 缓存 + A/B 测试 + 埋点 + 离线评估 + API 文档**。

### 15.1 技术栈新增

| 能力 | 组件 | 版本 |
|---|---|---|
| API 文档 | Knife4j + springdoc-openapi | 4.5.0 / 2.6.0 |
| 缓存 | Spring Data Redis + Lettuce | (Spring Boot 3.3) |
| 异步埋点 | `@EnableAsync` + 自定义线程池 | (JDK 17) |
| A/B 测试 | 自研 hash 分流器 | - |
| 评估 | 离线 NDCG + 埋点聚合 | - |

### 15.2 接口清单(新增/扩展)

| 模块 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 文档 | GET | `/doc.html` | Knife4j 增强 UI |
| 文档 | GET | `/swagger-ui.html` | 原生 Swagger UI |
| 文档 | GET | `/v3/api-docs` | OpenAPI JSON |
| 推荐 | POST | `/api/recommend/feedback` | 埋点反馈(CLICK/ORDER) |
| 评估 | GET | `/api/recommend/eval/ab?windowDays=7` | A/B 变体埋点统计 |
| 评估 | GET | `/api/recommend/eval/ndcg?topN=10` | 离线 NDCG 评估 |

### 15.3 Redis 缓存设计

Key 命名:

```
recommend:hot:{limit}                热门
recommend:high:{limit}               高分
recommend:scenario:{scenario}:{uid|none}:{limit}   场景
recommend:user:{userId}:{limit}:{abVariant}        个性化
recommend:mixed:{userId}:{limit}:{abVariant}       混合
```

TTL(可在 `application.yml` 调):

| 类型 | 默认 TTL |
|---|---|
| 热门 | 5 min |
| 高分 | 10 min |
| 场景 | 10 min |
| 个性化 | 3 min |
| 混合 | 3 min |

失效策略:
- **TTL 自然过期**(主)
- **事件驱动失效**:用户收藏 / 下单 / 评论时通过 `ApplicationEventPublisher` 发布 `UserBehaviorEvent`,`RecommendCacheInvalidator` 异步失效该用户的 `recommend:user:*` 和 `recommend:mixed:*`

### 15.4 A/B 测试

分流策略:
- 基于 `userId` 哈希值,`hash % 100 < strategyAWeight` 落到 A,否则 B
- 同一用户始终走同一变体(粘性)
- 策略 A: 0.6 内容 + 0.4 协同
- 策略 B: 0.7 内容 + 0.2 协同(强内容弱协同,适合内容充足但行为稀疏)

配置:
```yaml
recommend:
  ab-test:
    enabled: true
    strategy-a-weight: 50
    strategy-b-weight: 50
```

### 15.5 埋点设计

`recommend_exposure_log` 表字段:
- `trace_id` 一次推荐请求的唯一 ID
- `scenario` 推荐场景(HOT / HIGH_SCORE / PERSONALIZED / MIXED / SCENE:LUNCH)
- `ab_variant` A/B 变体
- `dish_id` 菜品
- `rank_position` 推荐位次(1-based)
- `score` 推荐分
- `action` IMPRESSION / CLICK / ORDER
- `user_id` 匿名时为 NULL
- `request_uri` 触发接口
- `create_time` 毫秒级时间

埋点写入走 `recommendTrackerExecutor` 线程池,**不阻塞推荐响应**。

### 15.6 推荐评估

#### 15.6.1 A/B 埋点报告

```bash
curl http://localhost:8080/api/recommend/eval/ab?windowDays=7
```

返回:
```json
{
  "windowDays": 7,
  "variantA": {
    "impressions": 1234, "clicks": 87, "orders": 12,
    "ctr": 7.05, "cvr": 13.79, "uniqueDishes": 8, "uniqueUsers": 56
  },
  "variantB": { ... },
  "advice": "B 变体 CTR 高于 A 0.8 个百分点,建议逐步放量 B"
}
```

#### 15.6.2 离线 NDCG

```bash
curl http://localhost:8080/api/recommend/eval/ndcg?topN=10
```

抽样最近 30 天有 ORDER 行为的用户,以其历史点单菜品为 ground truth,
对推荐列表计算 NDCG@N,作为推荐质量离线指标。

### 15.7 全链路 traceId

- 拦截器 `TraceIdInterceptor` 为推荐接口注入 `X-Trace-Id`(请求头传入或自动生成 16 位)
- 响应头回传 `X-Trace-Id`,便于排障
- 埋点表里也带 `trace_id`,可关联到一次推荐请求的全链路(曝光 → 点击 → 转化)

### 15.8 推荐变体扩展(下一阶段)

- 引入 ALS 矩阵分解做隐语义协同过滤
- 加入时间衰减(`time_decay = exp(-(now - create_time) / 14d)`)
- 引入 Mahout 或 Faust 做流式重算
- 接入 Prometheus 监控 + Grafana 大盘

## 16. 第六阶段 - 计划

下一阶段可选方向:

- 引入 Redis Stream 做推荐事件流,实现近实时特征更新
- 引入 Caffeine 做 L1 + Redis L2 二级缓存
- 接入 Prometheus + Micrometer 暴露 `/actuator/prometheus`
- 集成 Jaeger/SkyWalking 做分布式追踪(替代自研 traceId)
- 引入 Spring Retry 做推荐容错降级

