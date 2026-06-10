package com.example.shitang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推荐埋点
 * 一次 IMPRESSION 可能对应多个菜品(整个推荐列表)
 * 每次 CLICK/ORDER 只对应一个菜品
 */
@Data
@TableName("recommend_exposure_log")
public class RecommendExposureLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String traceId;
    private Long userId;
    private String scenario;
    private String abVariant;
    private Long dishId;
    private Integer rankPosition;
    private BigDecimal score;
    /** IMPRESSION / CLICK / ORDER */
    private String action;
    private String requestUri;
    private LocalDateTime createTime;
}
