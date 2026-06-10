package com.example.shitang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shitang.common.Result;
import com.example.shitang.entity.Dish;
import com.example.shitang.entity.Orders;
import com.example.shitang.entity.User;
import com.example.shitang.service.DishService;
import com.example.shitang.service.OrderService;
import com.example.shitang.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    private final DishService dishService;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public Result<StatsVO> stats() {
        StatsVO vo = new StatsVO();
        vo.setDishCount((int) dishService.count());
        vo.setOrderCount((int) orderService.count());
        vo.setUserCount((int) userService.count());
        BigDecimal revenue = orderService.list(new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getStatus, "PAID"))
                .stream()
                .map(Orders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setRevenue(revenue);
        return Result.success(vo);
    }

    @Data
    public static class StatsVO {
        private Integer dishCount;
        private Integer orderCount;
        private Integer userCount;
        private BigDecimal revenue;
    }
}
