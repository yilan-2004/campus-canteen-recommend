package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.entity.User;
import com.example.shitang.entity.UserCart;
import com.example.shitang.mapper.UserCartMapper;
import com.example.shitang.service.CartService;
import com.example.shitang.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<UserCartMapper, UserCart> implements CartService {
    private final UserService userService;

    @Override
    public List<UserCart> getMyCart() {
        Long userId = userService.currentUserEntity().getId();
        return list(new LambdaQueryWrapper<UserCart>()
                .eq(UserCart::getUserId, userId)
                .orderByDesc(UserCart::getUpdateTime));
    }

    @Override
    public UserCart addItem(Long dishId, Integer quantity) {
        Long userId = userService.currentUserEntity().getId();
        UserCart existing = getOne(new LambdaQueryWrapper<UserCart>()
                .eq(UserCart::getUserId, userId)
                .eq(UserCart::getDishId, dishId));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUpdateTime(LocalDateTime.now());
            updateById(existing);
            return existing;
        }
        UserCart cart = new UserCart();
        cart.setUserId(userId);
        cart.setDishId(dishId);
        cart.setQuantity(quantity);
        cart.setCreateTime(LocalDateTime.now());
        cart.setUpdateTime(LocalDateTime.now());
        save(cart);
        return cart;
    }

    @Override
    public void updateItem(Long dishId, Integer quantity) {
        Long userId = userService.currentUserEntity().getId();
        UserCart existing = getOne(new LambdaQueryWrapper<UserCart>()
                .eq(UserCart::getUserId, userId)
                .eq(UserCart::getDishId, dishId));
        if (existing != null) {
            if (quantity <= 0) {
                removeById(existing.getId());
            } else {
                existing.setQuantity(quantity);
                existing.setUpdateTime(LocalDateTime.now());
                updateById(existing);
            }
        }
    }

    @Override
    public void removeItem(Long dishId) {
        Long userId = userService.currentUserEntity().getId();
        remove(new LambdaQueryWrapper<UserCart>()
                .eq(UserCart::getUserId, userId)
                .eq(UserCart::getDishId, dishId));
    }

    @Override
    public void clearMyCart() {
        Long userId = userService.currentUserEntity().getId();
        remove(new LambdaQueryWrapper<UserCart>().eq(UserCart::getUserId, userId));
    }
}
