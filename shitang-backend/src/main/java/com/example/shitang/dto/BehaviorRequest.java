package com.example.shitang.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BehaviorRequest {
    @NotNull(message = "菜品ID不能为空")
    private Long dishId;

    @NotNull(message = "行为类型不能为空")
    private String behaviorType;

    private BigDecimal behaviorWeight;
}
