package com.example.shitang.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishRequest {
    @NotNull(message = "所属窗口不能为空")
    private Long windowId;

    @NotNull(message = "菜品分类不能为空")
    private Long categoryId;

    @NotBlank(message = "菜品名称不能为空")
    private String name;

    private String image;

    @NotNull(message = "菜品价格不能为空")
    @DecimalMin(value = "0.01", message = "菜品价格必须大于0")
    private BigDecimal price;

    private String description;
    private String taste;
    private Integer calories;
    private Integer status;
}
