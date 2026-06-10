package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.dto.OrderCreateRequest;
import com.example.shitang.entity.Orders;
import com.example.shitang.vo.OrderVO;

import java.util.List;

public interface OrderService extends IService<Orders> {
    OrderVO createOrder(OrderCreateRequest request);
    List<OrderVO> listMine();
    List<OrderVO> listAdmin();
    OrderVO updateStatus(Long id, String status);
}
