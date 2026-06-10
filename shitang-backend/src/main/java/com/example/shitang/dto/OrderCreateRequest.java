package com.example.shitang.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    @Valid
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemRequest> items;
}
