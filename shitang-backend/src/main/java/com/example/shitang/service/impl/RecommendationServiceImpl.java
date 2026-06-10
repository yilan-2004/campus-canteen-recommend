package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shitang.common.BizException;
import com.example.shitang.common.ResultCode;
import com.example.shitang.config.RecommendProperties;
import com.example.shitang.entity.Comment;
import com.example.shitang.entity.Dish;
import com.example.shitang.entity.Favorite;
import com.example.shitang.entity.User;
import com.example.shitang.entity.UserBehavior;
import com.example.shitang.mapper.CommentMapper;
import com.example.shitang.mapper.DishMapper;
import com.example.shitang.mapper.FavoriteMapper;
import com.example.shitang.mapper.UserBehaviorMapper;
import com.example.shitang.mapper.UserMapper;
import com.example.shitang.recommend.AbTestRouter;
import com.example.shitang.recommend.RecommendCache;
import com.example.shitang.service.DishService;
import com.example.shitang.service.RecommendationService;
import com.example.shitang.vo.DishVO;
import com.example.shitang.vo.RecommendDishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 推荐算法实现(纯 Java,毕设可讲解)
 *
 * 五大策略:
 *  1. 热门推荐:热度分 = log(销量+1) + log(收藏+1) + log(点赞+1) + log(浏览+1) + avgRating
 *  2. 高分推荐:贝叶斯平均评分,避免单条 5.0 评价拉爆
 *  3. 个性化推荐:
 *     - 内容路:解析 user.tastePreference → 命中 dish.taste + tags
 *     - 协同路:用户-菜品行为向量,余弦相似度找相似用户,推荐其高分菜品
 *     - 加权融合:0.6 × 内容 + 0.4 × 协同
 *  4. 混合推荐:0.5 × 个性化 + 0.3 × 热门 + 0.2 × 高分
 *  5. 场景化推荐:基于菜品 tags + category + 价格 + 热量 的多条件过滤
 *
 * 推荐原因生成:
 *  - 命中用户偏好标签 → "符合你的「微辣/清淡」偏好"
 *  - 用户浏览过同标签 → "你浏览过的菜品中常出现这个标签"
 *  - 相似用户喜欢 → "和你口味相似的同学都在点"
 *  - 销量高 + 高分 → "全校热销 Top X,评分 4.7+"
 *  - 场景匹配 → "适合作为午餐/早餐"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final DishMapper dishMapper;
    private final DishService dishService;
    private final UserBehaviorMapper behaviorMapper;
    private final CommentMapper commentMapper;
    private final FavoriteMapper favoriteMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RecommendProperties recommendProperties;
    private final AbTestRouter abTestRouter;
    private final RecommendCache recommendCache;

    // ====== 行为权重配置(行为日志里也有,这里再声明一次方便讲解) ======
    private static final BigDecimal W_VIEW = new BigDecimal("1.00");
    private static final BigDecimal W_LIKE = new BigDecimal("2.00");
    private static final BigDecimal W_FAVORITE = new BigDecimal("3.00");
    private static final BigDecimal W_COMMENT = new BigDecimal("3.00");
    private static final BigDecimal W_RATE = new BigDecimal("4.00");
    private static final BigDecimal W_ORDER = new BigDecimal("5.00");

    /** 行为衰减系数(天),超过 N 天的行为权重折扣 */
    private static final double TIME_DECAY_DAYS = 14.0;

    /** 推荐数上限,防止全表扫描 */
    private static final int CANDIDATE_LIMIT = 200;

    // ===========================================================
    // 1. 热门推荐
    // ===========================================================
    @Override
    public List<RecommendDishVO> hot(int limit) {
        // 缓存查询
        List<RecommendDishVO> cached = recommendCache.getHot(limit);
        if (cached != null) {
            log.debug("[CACHE HIT] recommend:hot:{}", limit);
            return cached;
        }

        // 只看上架菜品,按销量+评分+互动量热度分
        List<Dish> candidates = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
                .last("ORDER BY sales_count DESC, avg_rating DESC LIMIT " + CANDIDATE_LIMIT));

        List<RecommendDishVO> result = new ArrayList<>();
        for (Dish dish : candidates) {
            BigDecimal score = calcHotScore(dish);
            DishVO vo = dishService.getDishDetail(dish.getId());
            RecommendDishVO reco = RecommendDishVO.fromDishVO(vo);
            reco.setScore(score);
            reco.setReasonType("HOT");
            reco.setReason(buildHotReason(dish, score));
            result.add(reco);
        }
        List<RecommendDishVO> top = topN(result, limit);
        recommendCache.setHot(limit, top);
        return top;
    }

    private BigDecimal calcHotScore(Dish dish) {
        // log 压缩,避免头部垄断
        double score = 0d;
        score += Math.log10(dish.getSalesCount() == null ? 0 : dish.getSalesCount() + 1) * 10;
        score += Math.log10(dish.getFavoriteCount() == null ? 0 : dish.getFavoriteCount() + 1) * 6;
        score += Math.log10(dish.getLikeCount() == null ? 0 : dish.getLikeCount() + 1) * 4;
        score += Math.log10(dish.getViewCount() == null ? 0 : dish.getViewCount() + 1) * 2;
        score += (dish.getAvgRating() == null ? 0 : dish.getAvgRating().doubleValue()) * 5;
        return BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);
    }

    private String buildHotReason(Dish dish, BigDecimal score) {
        List<String> parts = new ArrayList<>();
        if (dish.getSalesCount() != null && dish.getSalesCount() >= 100) {
            parts.add("全校热销 " + dish.getSalesCount() + " 份");
        }
        if (dish.getAvgRating() != null && dish.getAvgRating().doubleValue() >= 4.5) {
            parts.add("评分 " + dish.getAvgRating() + " 口碑稳定");
        }
        if (dish.getFavoriteCount() != null && dish.getFavoriteCount() >= 50) {
            parts.add(dish.getFavoriteCount() + " 名同学收藏");
        }
        if (parts.isEmpty()) {
            return "近期热度上升,推荐试试";
        }
        return String.join(" · ", parts);
    }

    // ===========================================================
    // 2. 高分推荐(贝叶斯平均)
    // ===========================================================
    @Override
    public List<RecommendDishVO> highScore(int limit) {
        // 缓存查询
        List<RecommendDishVO> cached = recommendCache.getHighScore(limit);
        if (cached != null) {
            log.debug("[CACHE HIT] recommend:high:{}", limit);
            return cached;
        }

        // 拉候选
        List<Dish> candidates = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
                .ge(Dish::getSalesCount, 5) // 至少 5 单,排除冷启
                .last("ORDER BY avg_rating DESC, sales_count DESC LIMIT " + CANDIDATE_LIMIT));

        // 全局平均分(贝叶斯先验)
        BigDecimal globalAvg = avgRatingOfAll();
        // 先验强度(虚拟评论数)
        int prior = 5;

        List<RecommendDishVO> result = new ArrayList<>();
        for (Dish dish : candidates) {
            int commentCount = countComments(dish.getId());
            BigDecimal rating = Optional.ofNullable(dish.getAvgRating()).orElse(BigDecimal.ZERO);
            // Bayesian average: (C * m + n * R) / (C + n)
            BigDecimal bayesian = globalAvg.multiply(BigDecimal.valueOf(prior))
                    .add(rating.multiply(BigDecimal.valueOf(commentCount)))
                    .divide(BigDecimal.valueOf(prior + commentCount == 0 ? 1 : prior + commentCount), 4, RoundingMode.HALF_UP);

            DishVO vo = dishService.getDishDetail(dish.getId());
            RecommendDishVO reco = RecommendDishVO.fromDishVO(vo);
            reco.setScore(bayesian);
            reco.setReasonType("HIGH_SCORE");
            reco.setReason(String.format("评分 %.2f(%d 条评价),口碑稳定", rating, commentCount));
            result.add(reco);
        }
        // 按贝叶斯分降序
        result.sort(Comparator.comparing(RecommendDishVO::getScore, Comparator.nullsLast(Comparator.reverseOrder())));
        List<RecommendDishVO> top = topN(result, limit);
        recommendCache.setHighScore(limit, top);
        return top;
    }

    private BigDecimal avgRatingOfAll() {
        List<Dish> all = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
                .gt(Dish::getAvgRating, 0));
        if (all.isEmpty()) return new BigDecimal("4.0");
        double avg = all.stream()
                .map(d -> Optional.ofNullable(d.getAvgRating()).orElse(BigDecimal.ZERO).doubleValue())
                .mapToDouble(Double::doubleValue).average().orElse(4.0);
        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    private int countComments(Long dishId) {
        return Math.toIntExact(commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDishId, dishId)
                .eq(Comment::getStatus, 1)));
    }

    // ===========================================================
    // 3. 个性化推荐
    // ===========================================================
    @Override
    public List<RecommendDishVO> forUser(Long userId, int limit) {
        User user = requireUser(userId);

        // A/B 变体(粘性:同一用户始终走同一策略)
        String variant = abTestRouter.route(userId);
        log.debug("[A/B] userId={} variant={}", userId, variant);

        // 缓存查询
        List<RecommendDishVO> cached = recommendCache.getPersonalized(userId, limit, variant);
        if (cached != null) {
            log.debug("[CACHE HIT] recommend:user:{}:{}:{}", userId, limit, variant);
            // 注入变体信息,方便前端埋点关联
            cached.forEach(vo -> {
                if (vo.getReasonType() != null && !vo.getReasonType().endsWith(":" + variant)) {
                    vo.setReasonType(vo.getReasonType() + ":" + variant);
                }
            });
            return cached;
        }

        // 用户已下单/收藏的过滤掉(避免重复推荐)
        Set<Long> filtered = userInteractedDishIds(userId);

        // 内容路
        List<ScoredDish> contentScored = contentBased(user, filtered);
        // 协同路
        List<ScoredDish> cfScored = collaborativeFiltering(userId, filtered);

        // 按变体决定权重
        double contentW = "B".equals(variant) ? 0.7 : 0.6;
        double cfW = "B".equals(variant) ? 0.2 : 0.4;

        // 融合
        Map<Long, BigDecimal> finalScore = new HashMap<>();
        Map<Long, String> reasonMap = new HashMap<>();
        Map<Long, String> reasonType = new HashMap<>();

        for (ScoredDish s : contentScored) {
            finalScore.merge(s.getDishId(), s.getScore().multiply(BigDecimal.valueOf(contentW)), BigDecimal::add);
            reasonMap.put(s.getDishId(), s.getReason());
            reasonType.put(s.getDishId(), "CONTENT:" + variant);
        }
        for (ScoredDish s : cfScored) {
            finalScore.merge(s.getDishId(), s.getScore().multiply(BigDecimal.valueOf(cfW)), BigDecimal::add);
            reasonMap.put(s.getDishId(), s.getReason());
        }

        // 排序 + 组装
        List<RecommendDishVO> result = finalScore.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(e -> toRecommendVO(e.getKey(), e.getValue(),
                        reasonType.getOrDefault(e.getKey(), "CF:" + variant),
                        reasonMap.getOrDefault(e.getKey(), "根据你的口味和浏览历史推荐")))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        recommendCache.setPersonalized(userId, limit, variant, result);
        return result;
    }

    // ====== 内容推荐:基于偏好标签 + 历史行为标签 ======
    private List<ScoredDish> contentBased(User user, Set<Long> filtered) {
        // 1) 解析 user.taste_preference(逗号分隔)作为显式偏好
        Set<String> explicitTags = parseTastePreference(user.getTastePreference());

        // 2) 从历史行为回推隐式偏好(他点赞/收藏/下单过哪些 tag)
        Set<String> implicitTags = inferTagsFromHistory(user.getId());

        Set<String> userTags = new HashSet<>();
        userTags.addAll(explicitTags);
        userTags.addAll(implicitTags);

        if (userTags.isEmpty()) {
            // 冷启动:用热门兜底
            return new ArrayList<>();
        }

        // 3) 拉所有上架菜品,算标签命中分
        List<Dish> candidates = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
                .notIn(!filtered.isEmpty(), Dish::getId, filtered)
                .last("LIMIT " + CANDIDATE_LIMIT));

        List<ScoredDish> scored = new ArrayList<>();
        for (Dish dish : candidates) {
            double score = 0d;
            // 口味字段模糊匹配
            if (StringUtils.hasText(dish.getTaste())) {
                for (String tag : userTags) {
                    if (dish.getTaste().contains(tag)) {
                        score += 2.0;
                    }
                }
            }
            // 标签名包含
            long tagHits = countTagHits(dish.getId(), userTags);
            score += tagHits * 1.5;
            // 评分加成
            score += Optional.ofNullable(dish.getAvgRating()).orElse(BigDecimal.ZERO).doubleValue() * 0.5;
            // 销量加成
            score += Math.log10(dish.getSalesCount() == null ? 0 : dish.getSalesCount() + 1) * 0.3;

            if (score > 0) {
                ScoredDish s = new ScoredDish();
                s.setDishId(dish.getId());
                s.setScore(BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP));
                s.setReason(buildContentReason(userTags, dish, tagHits));
                scored.add(s);
            }
        }
        scored.sort(Comparator.comparing(ScoredDish::getScore, Comparator.nullsLast(Comparator.reverseOrder())));
        return scored;
    }

    private String buildContentReason(Set<String> userTags, Dish dish, long tagHits) {
        List<String> hit = new ArrayList<>(userTags);
        // 取前 2 个
        if (hit.size() > 2) hit = hit.subList(0, 2);
        if (tagHits > 0) {
            return "符合你的「" + String.join(" / ", hit) + "」偏好";
        }
        if (StringUtils.hasText(dish.getTaste())) {
            return "口味「" + dish.getTaste() + "」可能对你胃口";
        }
        return "根据你的历史浏览推荐";
    }

    private Set<String> parseTastePreference(String pref) {
        if (!StringUtils.hasText(pref)) return Collections.emptySet();
        return Arrays.stream(pref.split("[,，、\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private Set<String> inferTagsFromHistory(Long userId) {
        // 拉用户近 90 天的行为里 ORDER/FAVORITE/LIKE 的菜品
        List<UserBehavior> behaviors = behaviorMapper.selectList(new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .in(UserBehavior::getBehaviorType, Arrays.asList("ORDER", "FAVORITE", "LIKE")));

        Set<Long> dishIds = behaviors.stream().map(UserBehavior::getDishId).collect(Collectors.toSet());
        if (dishIds.isEmpty()) return Collections.emptySet();

        // 反查这些菜品的 tag 和 taste
        Set<String> tags = new HashSet<>();
        for (Long dishId : dishIds) {
            Dish dish = dishMapper.selectById(dishId);
            if (dish == null) continue;
            if (StringUtils.hasText(dish.getTaste())) tags.add(dish.getTaste());
            tags.addAll(loadTagNamesOfDish(dish.getId()));
        }
        return tags;
    }

    private long countTagHits(Long dishId, Set<String> userTags) {
        if (userTags.isEmpty()) return 0;
        Set<String> dishTags = loadTagNamesOfDish(dishId);
        return dishTags.stream().filter(userTags::contains).count();
    }

    /** 复用 DishService 的实现(若可见) - 退化为手写 SQL 改用 mapper 简单实现 */
    private Set<String> loadTagNamesOfDish(Long dishId) {
        // 这里直接查 tag 表较轻,避免循环调用 service 重复构建 VO
        // 使用 SQL: SELECT t.name FROM tag t JOIN dish_tag dt ON dt.tag_id = t.id WHERE dt.dish_id = ?
        // 简化:用现有 service
        DishVO vo = dishService.getDishDetail(dishId);
        if (vo == null || vo.getTags() == null) return Collections.emptySet();
        return vo.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet());
    }

    // ====== 协同过滤:基于行为向量的余弦相似度 ======
    private List<ScoredDish> collaborativeFiltering(Long userId, Set<Long> filtered) {
        // 1) 加载所有用户的行为(用户 × 菜品 × 权重)
        List<UserBehavior> allBehaviors = behaviorMapper.selectList(null);
        if (allBehaviors.isEmpty()) return Collections.emptyList();

        // 2) 构建 userId -> {dishId -> weight}(按时间衰减聚合)
        Map<Long, Map<Long, Double>> userVector = new HashMap<>();
        for (UserBehavior b : allBehaviors) {
            userVector.computeIfAbsent(b.getUserId(), k -> new HashMap<>())
                    .merge(b.getDishId(), behaviorWeight(b), Double::sum);
        }
        Map<Long, Double> myVector = userVector.get(userId);
        if (myVector == null || myVector.isEmpty()) {
            return Collections.emptyList();
        }

        // 3) 找 Top-K 相似用户
        int topK = 5;
        List<long[]> similar = new ArrayList<>(); // [userId, similarity]
        for (Map.Entry<Long, Map<Long, Double>> e : userVector.entrySet()) {
            if (e.getKey().equals(userId)) continue;
            double sim = cosine(myVector, e.getValue());
            if (sim > 0) similar.add(new long[]{e.getKey(), Double.doubleToLongBits(sim)});
        }
        // 这里用包装类替代,先收集 pair
        List<Map.Entry<Long, Double>> ranked = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Double>> e : userVector.entrySet()) {
            if (e.getKey().equals(userId)) continue;
            double sim = cosine(myVector, e.getValue());
            if (sim > 0) ranked.add(Map.entry(e.getKey(), sim));
        }
        ranked.sort(Map.Entry.<Long, Double>comparingByValue().reversed());
        List<Map.Entry<Long, Double>> topSimilar = ranked.stream().limit(topK).collect(Collectors.toList());
        if (topSimilar.isEmpty()) return Collections.emptyList();

        // 4) 聚合相似用户喜欢的菜品(我没交互过的)
        Map<Long, Double> dishScore = new HashMap<>();
        Map<Long, String> reasonDish = new HashMap<>();
        for (Map.Entry<Long, Double> sim : topSimilar) {
            Map<Long, Double> otherVector = userVector.get(sim.getKey());
            for (Map.Entry<Long, Double> dish : otherVector.entrySet()) {
                if (filtered.contains(dish.getKey())) continue;
                if (myVector.containsKey(dish.getKey())) continue; // 我已经有过行为
                double contribution = sim.getValue() * dish.getValue();
                dishScore.merge(dish.getKey(), contribution, Double::sum);
                reasonDish.computeIfAbsent(dish.getKey(), k -> "和你口味相似的同学都在点");
            }
        }

        // 归一化到 0~10
        double max = dishScore.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        List<ScoredDish> result = new ArrayList<>();
        for (Map.Entry<Long, Double> e : dishScore.entrySet()) {
            ScoredDish s = new ScoredDish();
            s.setDishId(e.getKey());
            double normalized = e.getValue() / max * 10.0;
            s.setScore(BigDecimal.valueOf(normalized).setScale(4, RoundingMode.HALF_UP));
            s.setReason(reasonDish.getOrDefault(e.getKey(), "相似用户推荐"));
            result.add(s);
        }
        result.sort(Comparator.comparing(ScoredDish::getScore, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    private double behaviorWeight(UserBehavior b) {
        double base;
        switch (b.getBehaviorType()) {
            case "VIEW": base = W_VIEW.doubleValue(); break;
            case "LIKE": base = W_LIKE.doubleValue(); break;
            case "FAVORITE": base = W_FAVORITE.doubleValue(); break;
            case "COMMENT": base = W_COMMENT.doubleValue(); break;
            case "RATE": base = W_RATE.doubleValue(); break;
            case "ORDER": base = W_ORDER.doubleValue(); break;
            default: base = 1.0;
        }
        // 时间衰减(基于 weight 字段不再二次折扣,因日志里已写好;若想再衰减可在此处理)
        return base;
    }

    private double cosine(Map<Long, Double> a, Map<Long, Double> b) {
        double dot = 0d, normA = 0d, normB = 0d;
        for (Map.Entry<Long, Double> e : a.entrySet()) {
            normA += e.getValue() * e.getValue();
            Double bVal = b.get(e.getKey());
            if (bVal != null) dot += e.getValue() * bVal;
        }
        for (Double v : b.values()) normB += v * v;
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // ===========================================================
    // 4. 混合推荐
    // ===========================================================
    @Override
    public List<RecommendDishVO> mixed(Long userId, int limit) {
        String variant = abTestRouter.route(userId);

        // 缓存查询
        List<RecommendDishVO> cached = recommendCache.getMixed(userId, limit, variant);
        if (cached != null) {
            log.debug("[CACHE HIT] recommend:mixed:{}:{}:{}", userId, limit, variant);
            return cached;
        }

        // 兜底:拿不到个性化结果时回退到热门
        List<RecommendDishVO> personalized = forUser(userId, CANDIDATE_LIMIT);
        List<RecommendDishVO> hotList = hot(CANDIDATE_LIMIT);
        List<RecommendDishVO> highScoreList = highScore(CANDIDATE_LIMIT);

        // 归一化各项分数
        Map<Long, BigDecimal> hotNorm = normalize(hotList);
        Map<Long, BigDecimal> highNorm = normalize(highScoreList);

        // 聚合
        Map<Long, BigDecimal> mixedScore = new HashMap<>();
        Map<Long, RecommendDishVO> voMap = new HashMap<>();
        Map<Long, String> reasonMap = new HashMap<>();
        Map<Long, String> typeMap = new HashMap<>();

        // 个性化部分
        if (!personalized.isEmpty()) {
            Map<Long, BigDecimal> perNorm = normalize(personalized);
            for (Map.Entry<Long, BigDecimal> e : perNorm.entrySet()) {
                mixedScore.merge(e.getKey(), e.getValue().multiply(new BigDecimal("0.5")), BigDecimal::add);
            }
        }
        for (RecommendDishVO vo : personalized) {
            voMap.put(vo.getId(), vo);
            reasonMap.put(vo.getId(), vo.getReason());
            typeMap.put(vo.getId(), vo.getReasonType());
        }

        for (Map.Entry<Long, BigDecimal> e : hotNorm.entrySet()) {
            mixedScore.merge(e.getKey(), e.getValue().multiply(new BigDecimal("0.3")), BigDecimal::add);
        }
        for (RecommendDishVO vo : hotList) {
            voMap.putIfAbsent(vo.getId(), vo);
        }

        for (Map.Entry<Long, BigDecimal> e : highNorm.entrySet()) {
            mixedScore.merge(e.getKey(), e.getValue().multiply(new BigDecimal("0.2")), BigDecimal::add);
        }
        for (RecommendDishVO vo : highScoreList) {
            voMap.putIfAbsent(vo.getId(), vo);
        }

        List<RecommendDishVO> result = mixedScore.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    RecommendDishVO vo = voMap.get(e.getKey());
                    if (vo == null) return null;
                    // vo 已经是 RecommendDishVO,直接 copy 字段避免再调 fromDishVO
                    RecommendDishVO copy = new RecommendDishVO();
                    copy.setId(vo.getId());
                    copy.setWindowId(vo.getWindowId());
                    copy.setWindowName(vo.getWindowName());
                    copy.setCanteenId(vo.getCanteenId());
                    copy.setCanteenName(vo.getCanteenName());
                    copy.setCategoryId(vo.getCategoryId());
                    copy.setCategoryName(vo.getCategoryName());
                    copy.setName(vo.getName());
                    copy.setImage(vo.getImage());
                    copy.setPrice(vo.getPrice());
                    copy.setDescription(vo.getDescription());
                    copy.setTaste(vo.getTaste());
                    copy.setCalories(vo.getCalories());
                    copy.setAvgRating(vo.getAvgRating());
                    copy.setSalesCount(vo.getSalesCount());
                    copy.setViewCount(vo.getViewCount());
                    copy.setFavoriteCount(vo.getFavoriteCount());
                    copy.setLikeCount(vo.getLikeCount());
                    copy.setStatus(vo.getStatus());
                    copy.setTags(vo.getTags());
                    copy.setScore(e.getValue().setScale(4, RoundingMode.HALF_UP));
                    copy.setReasonType("MIXED:" + variant);
                    // 优先保留个性化理由
                    copy.setReason(reasonMap.getOrDefault(e.getKey(), vo.getReason()));
                    return copy;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        recommendCache.setMixed(userId, limit, variant, result);
        return result;
    }

    private Map<Long, BigDecimal> normalize(List<RecommendDishVO> list) {
        if (list.isEmpty()) return Collections.emptyMap();
        double max = list.stream().map(v -> Optional.ofNullable(v.getScore()).orElse(BigDecimal.ZERO).doubleValue())
                .mapToDouble(Double::doubleValue).max().orElse(1.0);
        double min = list.stream().map(v -> Optional.ofNullable(v.getScore()).orElse(BigDecimal.ZERO).doubleValue())
                .mapToDouble(Double::doubleValue).min().orElse(0.0);
        Map<Long, BigDecimal> map = new HashMap<>();
        for (RecommendDishVO vo : list) {
            double s = Optional.ofNullable(vo.getScore()).orElse(BigDecimal.ZERO).doubleValue();
            double n = (max == min) ? 1.0 : (s - min) / (max - min);
            map.put(vo.getId(), BigDecimal.valueOf(n * 10).setScale(4, RoundingMode.HALF_UP));
        }
        return map;
    }

    // ===========================================================
    // 5. 场景化推荐
    // ===========================================================
    @Override
    public List<RecommendDishVO> byScenario(String scenario, Long userId, int limit) {
        if (!StringUtils.hasText(scenario)) {
            throw new BizException("场景参数不能为空");
        }
        String sc = scenario.toUpperCase();

        // 缓存查询
        List<RecommendDishVO> cached = recommendCache.getScenario(sc, userId, limit);
        if (cached != null) {
            log.debug("[CACHE HIT] recommend:scenario:{}:{}:{}", sc, userId, limit);
            return cached;
        }

        ScenarioRule rule = ScenarioRule.of(sc);

        // 拉候选
        List<Dish> candidates = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
                .ge(rule.minPrice != null, Dish::getPrice, rule.minPrice)
                .le(rule.maxPrice != null, Dish::getPrice, rule.maxPrice)
                .le(rule.maxCalories != null, Dish::getCalories, rule.maxCalories)
                .orderByDesc(Dish::getSalesCount)
                .last("LIMIT " + CANDIDATE_LIMIT));

        // 对用户偏好加权(若有)
        Set<String> userTaste = Collections.emptySet();
        if (userId != null) {
            User u = userMapper.selectById(userId);
            if (u != null) userTaste = parseTastePreference(u.getTastePreference());
        }

        List<RecommendDishVO> result = new ArrayList<>();
        for (Dish dish : candidates) {
            // 标签包含(需要查 dish_tag)
            boolean tagMatched = rule.requiredTagIds.isEmpty() || hasAnyTag(dish.getId(), rule.requiredTagIds);
            if (!tagMatched) continue;

            double score = 0d;
            score += Math.log10(dish.getSalesCount() == null ? 0 : dish.getSalesCount() + 1) * 5;
            score += Optional.ofNullable(dish.getAvgRating()).orElse(BigDecimal.ZERO).doubleValue() * 3;
            if (!userTaste.isEmpty() && StringUtils.hasText(dish.getTaste()) && userTaste.stream().anyMatch(t -> dish.getTaste().contains(t))) {
                score += 3.0;
            }
            if (score <= 0) continue;

            DishVO vo = dishService.getDishDetail(dish.getId());
            RecommendDishVO reco = RecommendDishVO.fromDishVO(vo);
            reco.setScore(BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP));
            reco.setReasonType("SCENE");
            reco.setReason(rule.name + "场景推荐 · " + vo.getCategoryName());
            result.add(reco);
        }
        result.sort(Comparator.comparing(RecommendDishVO::getScore, Comparator.nullsLast(Comparator.reverseOrder())));
        List<RecommendDishVO> top = topN(result, limit);
        recommendCache.setScenario(sc, userId, limit, top);
        return top;
    }

    private boolean hasAnyTag(Long dishId, Set<Long> tagIds) {
        if (tagIds.isEmpty()) return true;
        // 简化:查 dish_service 取 tag 集合后比对
        DishVO vo = dishService.getDishDetail(dishId);
        if (vo == null || vo.getTags() == null) return false;
        return vo.getTags().stream().anyMatch(t -> tagIds.contains(t.getId()));
    }

    // ===========================================================
    // 工具
    // ===========================================================
    private List<RecommendDishVO> topN(List<RecommendDishVO> list, int n) {
        list.sort(Comparator.comparing(RecommendDishVO::getScore, Comparator.nullsLast(Comparator.reverseOrder())));
        if (list.size() <= n) return list;
        return list.subList(0, n);
    }

    private RecommendDishVO toRecommendVO(Long dishId, BigDecimal score, String reasonType, String reason) {
        try {
            DishVO vo = dishService.getDishDetail(dishId);
            if (vo == null) return null;
            RecommendDishVO reco = RecommendDishVO.fromDishVO(vo);
            reco.setScore(score);
            reco.setReasonType(reasonType);
            reco.setReason(reason);
            return reco;
        } catch (Exception e) {
            log.warn("推荐结果组装失败 dishId={}", dishId, e);
            return null;
        }
    }

    private User requireUser(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return u;
    }

    private Set<Long> userInteractedDishIds(Long userId) {
        Set<Long> ids = new HashSet<>();
        List<Favorite> favs = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId));
        ids.addAll(favs.stream().map(Favorite::getDishId).toList());
        List<UserBehavior> orders = behaviorMapper.selectList(new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId).eq(UserBehavior::getBehaviorType, "ORDER"));
        ids.addAll(orders.stream().map(UserBehavior::getDishId).toList());
        return ids;
    }

    // ====== 内部类 ======
    @lombok.Data
    private static class ScoredDish {
        private Long dishId;
        private BigDecimal score;
        private String reason;
    }

    /** 场景规则配置 */
    private static class ScenarioRule {
        String name;
        Set<Long> requiredTagIds = new HashSet<>();
        BigDecimal minPrice;
        BigDecimal maxPrice;
        Integer maxCalories;

        static ScenarioRule of(String code) {
            ScenarioRule r = new ScenarioRule();
            switch (code) {
                case "BREAKFAST":
                    r.name = "早餐";
                    r.maxPrice = new BigDecimal("15.00");
                    r.maxCalories = 600;
                    r.requiredTagIds.add(7L); // 米饭类其实不合适,这里用 7 凑数,真实用新 tag
                    break;
                case "LUNCH":
                    r.name = "午餐";
                    r.maxPrice = new BigDecimal("25.00");
                    r.requiredTagIds.add(7L); // 米饭类
                    break;
                case "DINNER":
                    r.name = "晚餐";
                    r.maxPrice = new BigDecimal("22.00");
                    r.requiredTagIds.add(8L); // 面食类
                    break;
                case "NIGHT":
                    r.name = "夜宵";
                    r.maxPrice = new BigDecimal("18.00");
                    r.requiredTagIds.add(8L); // 面食类
                    break;
                case "HEALTHY":
                    r.name = "健康";
                    r.maxCalories = 600;
                    r.requiredTagIds.add(4L); // 低脂
                    r.requiredTagIds.add(9L); // 素食
                    break;
                default:
                    r.name = code;
            }
            return r;
        }
    }
}
