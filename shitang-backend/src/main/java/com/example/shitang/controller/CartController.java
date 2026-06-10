package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.entity.UserCart;
import com.example.shitang.service.CartService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public Result<List<UserCart>> myCart() {
        return Result.success(cartService.getMyCart());
    }

    @PostMapping
    public Result<UserCart> add(@RequestBody CartRequest request) {
        return Result.success(cartService.addItem(request.getDishId(), request.getQuantity() == null ? 1 : request.getQuantity()));
    }

    @PutMapping("/{dishId}")
    public Result<Void> update(@PathVariable Long dishId, @RequestBody CartRequest request) {
        cartService.updateItem(dishId, request.getQuantity());
        return Result.success();
    }

    @DeleteMapping("/{dishId}")
    public Result<Void> remove(@PathVariable Long dishId) {
        cartService.removeItem(dishId);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> clear() {
        cartService.clearMyCart();
        return Result.success();
    }

    @Data
    public static class CartRequest {
        private Long dishId;
        private Integer quantity;
    }
}
