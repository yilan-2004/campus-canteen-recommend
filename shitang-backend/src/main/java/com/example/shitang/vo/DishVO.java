package com.example.shitang.vo;

import com.example.shitang.entity.Dish;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DishVO {
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<TagVO> tags;
    private Boolean liked;
    private Boolean favorited;

    public static DishVO from(Dish dish) {
        DishVO vo = new DishVO();
        vo.setId(dish.getId());
        vo.setWindowId(dish.getWindowId());
        vo.setCategoryId(dish.getCategoryId());
        vo.setName(dish.getName());
        vo.setImage(dish.getImage());
        vo.setPrice(dish.getPrice());
        vo.setDescription(dish.getDescription());
        vo.setTaste(dish.getTaste());
        vo.setCalories(dish.getCalories());
        vo.setAvgRating(dish.getAvgRating());
        vo.setSalesCount(dish.getSalesCount());
        vo.setViewCount(dish.getViewCount());
        vo.setFavoriteCount(dish.getFavoriteCount());
        vo.setLikeCount(dish.getLikeCount());
        vo.setStatus(dish.getStatus());
        vo.setCreateTime(dish.getCreateTime());
        vo.setUpdateTime(dish.getUpdateTime());
        return vo;
    }
}
