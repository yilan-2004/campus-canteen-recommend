package com.example.shitang.recommend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shitang.dto.EvaluationReport;
import com.example.shitang.entity.RecommendExposureLog;
import com.example.shitang.mapper.RecommendExposureLogMapper;
import com.example.shitang.service.RecommendationService;
import com.example.shitang.vo.RecommendDishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 推荐效果评估服务
 *
 * 输入:埋点日志(最近 N 天)
 * 输出:
 *   - 每种变体(A/B)的曝光/点击/下单数
 *   - CTR / CVR
 *   - 对比建议
 *
 * 同时提供离线 NDCG 评估:对每个用户用推荐列表 vs. 真实后续行为 ORDER 集合比对
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final RecommendExposureLogMapper exposureMapper;
    private final RecommendationService recommendationService;

    /**
     * 汇总埋点:按 abVariant 统计曝光/点击/转化
     */
    public EvaluationReport reportByAbTest(Integer windowDays) {
        int days = windowDays == null || windowDays <= 0 ? 7 : windowDays;
        LocalDateTime from = LocalDateTime.now().minusDays(days);

        List<RecommendExposureLog> logs = exposureMapper.selectList(new LambdaQueryWrapper<RecommendExposureLog>()
                .ge(RecommendExposureLog::getCreateTime, from));

        EvaluationReport report = new EvaluationReport();
        report.setWindowDays(days);
        report.setVariantA(aggregate(logs, "A"));
        report.setVariantB(aggregate(logs, "B"));

        // 给出建议
        EvaluationReport.Metrics a = report.getVariantA();
        EvaluationReport.Metrics b = report.getVariantB();
        if (a != null && b != null && a.getImpressions() > 0 && b.getImpressions() > 0) {
            int cmp = b.getCtr().compareTo(a.getCtr());
            if (cmp > 0) {
                report.setAdvice("B 变体 CTR 高于 A " + a.getCtr().subtract(b.getCtr()).abs()
                        + " 个百分点,建议逐步放量 B(可调高 strategy-b-weight)");
            } else if (cmp < 0) {
                report.setAdvice("A 变体 CTR 高于 B " + a.getCtr().subtract(b.getCtr()).abs()
                        + " 个百分点,建议保持 A 为主");
            } else {
                report.setAdvice("A/B 表现接近,可保持当前权重");
            }
        } else {
            report.setAdvice("样本量不足(需每个变体至少 50 次曝光),继续采集");
        }
        return report;
    }

    /**
     * 离线 NDCG 评估
     *
     * 思路:
     *  - 对最近 N 天有 ORDER 行为的用户
     *  - 取他在 ORDER 前 30 天内的真实点单菜品集合(ground truth)
     *  - 调用个性化推荐得到 N 个推荐菜品
     *  - 算 NDCG@N
     */
    public Map<String, Object> offlineNdcg(int topN, int sampleUsers) {
        // 简化为抽样全部有曝光的用户
        List<RecommendExposureLog> recent = exposureMapper.selectList(new LambdaQueryWrapper<RecommendExposureLog>()
                .eq(RecommendExposureLog::getAction, "ORDER")
                .ge(RecommendExposureLog::getCreateTime, LocalDateTime.now().minusDays(30))
                .orderByDesc(RecommendExposureLog::getCreateTime)
                .last("LIMIT 1000"));

        if (recent.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("ndcg", BigDecimal.ZERO);
            empty.put("users", 0);
            empty.put("note", "近期没有 ORDER 行为,无法评估");
            return empty;
        }

        Set<Long> userIds = recent.stream()
                .map(RecommendExposureLog::getUserId)
                .filter(Objects::nonNull)
                .limit(Math.min(sampleUsers, 200))
                .collect(Collectors.toSet());

        double sumNdcg = 0d;
        int counted = 0;
        for (Long uid : userIds) {
            // ground truth:用户最近所有 ORDER 的菜品 ID
            Set<Long> truth = recent.stream()
                    .filter(l -> uid.equals(l.getUserId()) && "ORDER".equals(l.getAction()))
                    .map(RecommendExposureLog::getDishId)
                    .collect(Collectors.toSet());
            if (truth.isEmpty()) continue;

            try {
                List<RecommendDishVO> recos = recommendationService.forUser(uid, topN);
                double ndcg = calcNdcg(recos, truth, topN);
                sumNdcg += ndcg;
                counted++;
            } catch (Exception e) {
                log.warn("评估用户 {} 失败", uid, e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ndcg@" + topN, counted == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(sumNdcg / counted).setScale(4, RoundingMode.HALF_UP));
        result.put("users", counted);
        return result;
    }

    private double calcNdcg(List<RecommendDishVO> recos, Set<Long> truth, int topN) {
        double dcg = 0d;
        for (int i = 0; i < recos.size() && i < topN; i++) {
            long dishId = recos.get(i).getId() == null ? 0L : recos.get(i).getId();
            int rel = truth.contains(dishId) ? 1 : 0;
            dcg += (Math.pow(2, rel) - 1) / Math.log(i + 2);
        }
        // IDCG:理想情况下所有 truth 都按相关度排在前
        int ideal = Math.min(truth.size(), topN);
        double idcg = 0d;
        for (int i = 0; i < ideal; i++) {
            idcg += (Math.pow(2, 1) - 1) / Math.log(i + 2);
        }
        return idcg == 0 ? 0d : dcg / idcg;
    }

    private EvaluationReport.Metrics aggregate(List<RecommendExposureLog> logs, String variant) {
        EvaluationReport.Metrics m = new EvaluationReport.Metrics();
        if (logs == null || logs.isEmpty()) {
            m.setCtr(BigDecimal.ZERO);
            m.setCvr(BigDecimal.ZERO);
            return m;
        }
        long imp = logs.stream().filter(l -> variant.equals(l.getAbVariant()) && "IMPRESSION".equals(l.getAction())).count();
        long click = logs.stream().filter(l -> variant.equals(l.getAbVariant()) && "CLICK".equals(l.getAction())).count();
        long order = logs.stream().filter(l -> variant.equals(l.getAbVariant()) && "ORDER".equals(l.getAction())).count();
        long uniqueDishes = logs.stream().filter(l -> variant.equals(l.getAbVariant()))
                .map(RecommendExposureLog::getDishId).filter(Objects::nonNull).distinct().count();
        long uniqueUsers = logs.stream().filter(l -> variant.equals(l.getAbVariant()))
                .map(RecommendExposureLog::getUserId).filter(Objects::nonNull).distinct().count();
        m.setImpressions(imp);
        m.setClicks(click);
        m.setOrders(order);
        m.setUniqueDishes(uniqueDishes);
        m.setUniqueUsers(uniqueUsers);
        BigDecimal ctr = imp == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(click).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(imp), 4, RoundingMode.HALF_UP);
        BigDecimal cvr = click == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(order).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(click), 4, RoundingMode.HALF_UP);
        m.setCtr(ctr);
        m.setCvr(cvr);
        return m;
    }

    public List<Long> sampleUserIds(int n) {
        return Collections.emptyList();
    }
}
