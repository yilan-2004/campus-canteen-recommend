package com.example.shitang.service;

import com.example.shitang.vo.RecommendDishVO;

import java.util.List;

/**
 * 第四阶段:推荐算法服务
 *
 * 提供五类推荐:
 * 1. 热门推荐 hot()        - 基于销量+评分+互动量的热度排序
 * 2. 高分推荐 highScore()   - 按平均评分加权排序
 * 3. 个性化推荐 forUser()   - 内容过滤 + 协同过滤融合
 * 4. 混合推荐 mixed()       - 个性化 + 热门 + 评分三路加权
 * 5. 场景化推荐 byScenario()- 早/午/晚餐场景匹配
 */
public interface RecommendationService {

    /**
     * 热门推荐:全平台热度 Top N
     * @param limit 返回数量
     */
    List<RecommendDishVO> hot(int limit);

    /**
     * 高分推荐:按平均评分 × 评分人数加权
     * @param limit 返回数量
     */
    List<RecommendDishVO> highScore(int limit);

    /**
     * 个性化推荐:基于用户历史行为 + 偏好标签
     * @param userId 目标用户
     * @param limit 返回数量
     */
    List<RecommendDishVO> forUser(Long userId, int limit);

    /**
     * 混合推荐:对当前登录用户的加权融合结果
     * @param userId 目标用户
     * @param limit 返回数量
     */
    List<RecommendDishVO> mixed(Long userId, int limit);

    /**
     * 场景化推荐
     * @param scenario BREAKFAST / LUNCH / DINNER / NIGHT / HEALTHY
     * @param userId   可选,登录用户可结合偏好;传 null 则通用推荐
     * @param limit    返回数量
     */
    List<RecommendDishVO> byScenario(String scenario, Long userId, int limit);
}
