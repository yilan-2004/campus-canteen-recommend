# 校园食堂个性化推荐系统

基于用户行为分析与多策略融合算法的校园食堂点餐推荐平台，支持学生浏览菜品、个性化推荐、下单收藏，以及管理员后台管理。

## 技术栈

**后端：** Spring Boot 3.3.5 · Java 17 · MyBatis-Plus 3.5.7 · MySQL 8 · Redis · Spring Security + JWT · Knife4j 4.5 · Apache POI

**前端：** Vue 3.5 · Vite 6 · Element Plus 2.9 · Pinia 2.3 · Vue Router 4.5 · Axios · ECharts 5.5

## 项目结构

```
shitang/
├── shitang-backend/          # Spring Boot 后端
│   ├── src/main/java/com/example/shitang/
│   │   ├── controller/       # 19 个 REST 控制器
│   │   ├── service/          # 业务逻辑层（13 个 Service）
│   │   ├── mapper/           # MyBatis-Plus Mapper（12 个）
│   │   ├── entity/           # 数据实体（12 个）
│   │   ├── dto/              # 请求 DTO
│   │   ├── vo/               # 响应 VO
│   │   ├── config/           # 安全、Redis、CORS、异步等配置
│   │   └── utils/            # JWT 工具类
│   ├── sql/                  # 数据库脚本与种子数据
│   └── pom.xml
├── shitang-frontend/         # Vue 3 前端
│   ├── src/
│   │   ├── views/            # 学生端 10 页 + 管理端 9 页
│   │   ├── components/       # 6 个可复用组件
│   │   ├── api/              # API 请求封装
│   │   ├── router/           # 路由（含角色守卫）
│   │   ├── store/            # Pinia 状态管理
│   │   └── layouts/          # 学生端 / 管理端双 Layout
│   └── package.json
└── test_roles.py             # 角色测试脚本
```

## 核心功能

### 学生端

- 注册 / 登录（JWT 无状态认证）
- 菜品浏览、搜索、分类筛选
- **个性化推荐**（基于口味偏好 + 行为历史）
- 购物车（服务端同步）、收藏、下单
- 评论评分、通知中心
- 个人饮食偏好设置

### 管理端

- 数据看板（ECharts 统计图表）
- 菜品 / 分类 / 标签 / 食堂 / 窗口管理
- 订单管理、评论审核
- 用户管理（角色分配、状态控制）
- 推荐算法 A/B 测试评估（NDCG 离线指标）
- 数据导出（Excel）

## 推荐算法

系统实现了 5 种推荐策略，支持混合加权与 A/B 测试：

| 策略 | 接口 | 核心思路 |
|------|------|----------|
| 热门推荐 | `/api/recommend/hot` | log10 压缩的销量 / 收藏 / 点赞 / 浏览加权评分 |
| 高分推荐 | `/api/recommend/high-score` | 贝叶斯平均（先验强度 C=5），过滤冷启动菜品 |
| 个性化推荐 | `/api/recommend/user/{id}` | 内容路径 60%（标签匹配 + 口味偏好）+ 协同过滤 40%（余弦相似度 Top-5 用户） |
| 混合推荐 | `/api/recommend/mixed` | 0.5×个性化 + 0.3×热门 + 0.2×高分，Min-Max 归一化 |
| 场景推荐 | `/api/recommend/scenarios` | 早餐 / 午餐 / 晚餐 / 夜宵 / 健康轻食，按价格、热量、标签规则筛选 |

推荐链路支持 Redis 缓存（按策略分 TTL）、异步曝光日志追踪、行为事件驱动的缓存失效。

## 数据库设计

共 13 张表，覆盖用户、食堂、窗口、菜品、标签、行为、收藏、评论、订单、购物车、通知、推荐曝光日志。

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+

### 后端启动

```bash
# 1. 创建数据库并导入
mysql -u root -p < shitang-backend/sql/schema.sql
mysql -u root -p < shitang-backend/sql/recommend_log.sql
mysql -u root -p < shitang-backend/sql/V2_cart_notification.sql
mysql -u root -p < shitang-backend/sql/data.sql

# 2. 修改配置（数据库、Redis 连接）
#    编辑 shitang-backend/src/main/resources/application.yml

# 3. 启动
cd shitang-backend
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`，API 文档访问 `http://localhost:8080/doc.html`。

### 前端启动

```bash
cd shitang-frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`。

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 商户 | merchant1 / merchant2 | 123456 |
| 学生 | student1 / student2 | 123456 |

## License

MIT
