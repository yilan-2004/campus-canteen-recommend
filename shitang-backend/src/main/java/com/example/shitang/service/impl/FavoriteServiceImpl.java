package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.common.BizException;
import com.example.shitang.entity.Dish;
import com.example.shitang.entity.Favorite;
import com.example.shitang.entity.User;
import com.example.shitang.mapper.FavoriteMapper;
import com.example.shitang.recommend.RecommendCacheInvalidator;
import com.example.shitang.service.BehaviorService;
import com.example.shitang.service.DishService;
import com.example.shitang.service.FavoriteService;
import com.example.shitang.service.UserService;
import com.example.shitang.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {
    private final UserService userService;
    private final DishService dishService;
    private final BehaviorService behaviorService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteVO addFavorite(Long dishId) {
        User user = userService.currentUserEntity();
        Dish dish = requireAvailableDish(dishId);
        Favorite existed = getOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getId())
                .eq(Favorite::getDishId, dishId));
        if (existed != null) {
            return FavoriteVO.from(existed, dishService.getDishDetail(dishId));
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(user.getId());
        favorite.setDishId(dishId);
        favorite.setCreateTime(LocalDateTime.now());
        save(favorite);
        dish.setFavoriteCount((dish.getFavoriteCount() == null ? 0 : dish.getFavoriteCount()) + 1);
        dish.setUpdateTime(LocalDateTime.now());
        dishService.updateById(dish);
        behaviorService.record(user.getId(), dishId, "FAVORITE", BigDecimal.valueOf(3.00));
        eventPublisher.publishEvent(new RecommendCacheInvalidator.UserBehaviorEvent(user.getId(), "FAVORITE"));
        return FavoriteVO.from(favorite, dishService.getDishDetail(dishId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long dishId) {
        User user = userService.currentUserEntity();
        Favorite favorite = getOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getId())
                .eq(Favorite::getDishId, dishId));
        if (favorite == null) {
            return;
        }
        removeById(favorite.getId());
        Dish dish = dishService.getById(dishId);
        if (dish != null) {
            dish.setFavoriteCount(Math.max(0, (dish.getFavoriteCount() == null ? 0 : dish.getFavoriteCount()) - 1));
            dish.setUpdateTime(LocalDateTime.now());
            dishService.updateById(dish);
        }
    }

    @Override
    public List<FavoriteVO> listMine() {
        Long userId = userService.currentUserEntity().getId();
        return list(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime))
                .stream()
                .map(favorite -> FavoriteVO.from(favorite, dishService.getDishDetail(favorite.getDishId())))
                .toList();
    }

    @Override
    public boolean hasFavorite(Long userId, Long dishId) {
        return count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getDishId, dishId)) > 0;
    }

    private Dish requireAvailableDish(Long dishId) {
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            throw new BizException("菜品不存在");
        }
        if (dish.getStatus() == null || dish.getStatus() != 1) {
            throw new BizException("菜品未上架");
        }
        return dish;
    }
}
