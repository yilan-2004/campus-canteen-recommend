package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.common.BizException;
import com.example.shitang.dto.BehaviorRequest;
import com.example.shitang.entity.Dish;
import com.example.shitang.entity.User;
import com.example.shitang.entity.UserBehavior;
import com.example.shitang.mapper.DishMapper;
import com.example.shitang.mapper.UserBehaviorMapper;
import com.example.shitang.service.BehaviorService;
import com.example.shitang.service.UserService;
import com.example.shitang.vo.BehaviorVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior> implements BehaviorService {
    private final UserService userService;
    private final DishMapper dishMapper;

    @Override
    public UserBehavior record(Long userId, Long dishId, String behaviorType, BigDecimal behaviorWeight) {
        if (dishMapper.selectById(dishId) == null) {
            throw new BizException("菜品不存在");
        }
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setDishId(dishId);
        behavior.setBehaviorType(behaviorType);
        behavior.setBehaviorWeight(behaviorWeight == null ? defaultWeight(behaviorType) : behaviorWeight);
        behavior.setCreateTime(LocalDateTime.now());
        save(behavior);
        return behavior;
    }

    @Override
    public UserBehavior recordCurrentUser(BehaviorRequest request) {
        User user = userService.currentUserEntity();
        return record(user.getId(), request.getDishId(), request.getBehaviorType(), request.getBehaviorWeight());
    }

    @Override
    public List<BehaviorVO> listByUser(Long userId) {
        return list(new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .orderByDesc(UserBehavior::getCreateTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<BehaviorVO> listMine() {
        return listByUser(userService.currentUserEntity().getId());
    }

    @Override
    public boolean hasBehavior(Long userId, Long dishId, String behaviorType) {
        return count(new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getDishId, dishId)
                .eq(UserBehavior::getBehaviorType, behaviorType)) > 0;
    }

    @Override
    public void removeBehavior(Long userId, Long dishId, String behaviorType) {
        remove(new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getDishId, dishId)
                .eq(UserBehavior::getBehaviorType, behaviorType));
    }

    private BehaviorVO toVO(UserBehavior behavior) {
        BehaviorVO vo = BehaviorVO.from(behavior);
        Dish dish = dishMapper.selectById(behavior.getDishId());
        if (dish != null) {
            vo.setDishName(dish.getName());
        }
        return vo;
    }

    private BigDecimal defaultWeight(String behaviorType) {
        return switch (behaviorType) {
            case "LIKE" -> BigDecimal.valueOf(2.00);
            case "FAVORITE" -> BigDecimal.valueOf(3.00);
            case "COMMENT" -> BigDecimal.valueOf(3.50);
            case "ORDER" -> BigDecimal.valueOf(5.00);
            case "RATE", "VIEW" -> BigDecimal.ONE;
            default -> BigDecimal.ONE;
        };
    }
}
