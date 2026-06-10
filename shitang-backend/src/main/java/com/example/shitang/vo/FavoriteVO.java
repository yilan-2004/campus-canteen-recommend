package com.example.shitang.vo;

import com.example.shitang.entity.Favorite;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteVO {
    private Long id;
    private Long userId;
    private Long dishId;
    private LocalDateTime createTime;
    private DishVO dish;

    public static FavoriteVO from(Favorite favorite, DishVO dish) {
        FavoriteVO vo = new FavoriteVO();
        vo.setId(favorite.getId());
        vo.setUserId(favorite.getUserId());
        vo.setDishId(favorite.getDishId());
        vo.setCreateTime(favorite.getCreateTime());
        vo.setDish(dish);
        return vo;
    }
}
