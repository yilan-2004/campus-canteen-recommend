package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.dto.OrderCreateRequest;
import com.example.shitang.service.OrderService;
import com.example.shitang.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.success(orderService.createOrder(request));
    }

    @GetMapping("/my")
    public Result<List<OrderVO>> my() {
        return Result.success(orderService.listMine());
    }

    @GetMapping("/admin")
    public Result<List<OrderVO>> admin() {
        return Result.success(orderService.listAdmin());
    }

    @PutMapping("/{id}/status")
    public Result<OrderVO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(orderService.updateStatus(id, status));
    }
}
