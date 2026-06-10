package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.entity.UserCart;

import java.util.List;

public interface CartService extends IService<UserCart> {
    List<UserCart> getMyCart();
    UserCart addItem(Long dishId, Integer quantity);
    void updateItem(Long dishId, Integer quantity);
    void removeItem(Long dishId);
    void clearMyCart();
}
