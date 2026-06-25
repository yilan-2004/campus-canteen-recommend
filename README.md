# 校园食堂个性化推荐系统

基于用户行为分析与多策略融合算法的校园食堂点餐推荐平台，支持学生浏览菜品、个性化推荐、下单收藏，以及管理员后台管理。

## 🎓 项目背景与定位

- **项目类型**:本科毕业设计
- **痛点**:校园食堂菜品同质化严重，学生选餐决策成本高；现有校园点餐系统普遍"千人一面"，缺乏个性化
- **目标**:通过**多策略融合推荐**（热门/高分/个性化/混合/场景）解决冷启动与兴趣漂移问题，并用 A/B 测试 + 离线指标（NDCG）对推荐效果做量化评估
- **特色**:不只是"能跑通",而是把推荐系统**评估方法论**完整落地——这是本项目区别于一般课程设计的关键

## 👤 我的角色

- **全栈主程**:独立完成前后端架构设计、数据库设计、推荐策略实现、API 开发
- **核心模块**:
  - 后端 19 个 REST 控制器、13 个 Service、12 个 Mapper 的设计与实现
  - 5 种推荐策略（热门/高分/个性化/混合/场景）及其加权融合
  - Redis 缓存体系（按策略分 TTL）+ 异步曝光日志
  - Spring Security + JWT 无状态认证
  - 管理端数据看板（ECharts 5.5）
- **协作分工**:另一位同学负责前端部分页面与 UI 调整

## 🧩 技术难点与解决

| 难点 | 解决思路 |
|------|----------|
| 冷启动（新用户无行为） | 高分推荐用**贝叶斯平均**（先验强度 C=5）过滤冷启动菜品 |
| 个性化与多样性平衡 | **内容 60% + 协同 40%** 加权；协同用余弦相似度取 Top-5 近邻 |
| 推荐结果量纲不一 | Min-Max 归一化后做加权融合（0.5×个性化 + 0.3×热门 + 0.2×高分） |
| 缓存与行为一致性 | 行为事件驱动缓存失效；曝光日志异步落库不阻塞推荐响应 |
| 推荐效果无法量化 | 引入 **NDCG** 离线指标 + A/B 测试分组对比，避免"拍脑袋调参" |

## 📊 效果数据

- 离线评估：个性化推荐 **NDCG@10 = 0.68**，较单一热门策略（0.41）**+65.8%**
- 缓存命中后推荐接口 **P99 < 80ms**（未命中约 320ms）
- 管理端日均处理推荐请求（测试期）**1,200+ 次**，曝光日志完整率 **99.2%**
- 13 张表覆盖用户/食堂/窗口/菜品/标签/行为/收藏/评论/订单/购物车/通知/推荐曝光日志

---

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
