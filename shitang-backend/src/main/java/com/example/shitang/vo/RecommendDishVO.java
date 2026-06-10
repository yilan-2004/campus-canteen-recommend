package com.example.shitang.vo;

import com.example.shitang.entity.Dish;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推荐结果 VO
 *
 * - 复用 DishVO 的展示字段,减少前端重复定义
 * - score: 综合推荐分(越高越推荐)
 * - reason: 推荐原因中文文案(给前端直接展示)
 * - reasonTags: 命中原因标签,前端可用作角标
 */
@Data
public class RecommendDishVO {
    private Long id;
    private Long windowId;
    private String windowName;
    private Long canteenId;
    private String canteenName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String image;
    private BigDecimal price;
    private String description;
    private String taste;
    private Integer calories;
    private BigDecimal avgRating;
    private Integer salesCount;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer likeCount;
    private Integer status;
    private List<TagVO> tags;

    // ====== 推荐结果专属字段 ======
    /** 综合推荐分,保留 4 位小数 */
    private BigDecimal score;
    /** 推荐类型:HOT / CONTENT / CF / MIXED / SCENE */
    private String reasonType;
    /** 推荐原因中文文案 */
    private String reason;

    public static RecommendDishVO fromDishVO(DishVO dishVO) {
        RecommendDishVO vo = new RecommendDishVO();
        vo.setId(dishVO.getId());
        vo.setWindowId(dishVO.getWindowId());
        vo.setWindowName(dishVO.getWindowName());
        vo.setCanteenId(dishVO.getCanteenId());
        vo.setCanteenName(dishVO.getCanteenName());
        vo.setCategoryId(dishVO.getCategoryId());
        vo.setCategoryName(dishVO.getCategoryName());
        vo.setName(dishVO.getName());
        vo.setImage(dishVO.getImage());
        vo.setPrice(dishVO.getPrice());
        vo.setDescription(dishVO.getDescription());
        vo.setTaste(dishVO.getTaste());
        vo.setCalories(dishVO.getCalories());
        vo.setAvgRating(dishVO.getAvgRating());
        vo.setSalesCount(dishVO.getSalesCount());
        vo.setViewCount(dishVO.getViewCount());
        vo.setFavoriteCount(dishVO.getFavoriteCount());
        vo.setLikeCount(dishVO.getLikeCount());
        vo.setStatus(dishVO.getStatus());
        vo.setTags(dishVO.getTags());
        return vo;
    }
}
