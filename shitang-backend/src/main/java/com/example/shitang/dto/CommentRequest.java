package com.example.shitang.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommentRequest {
    @NotNull(message = "菜品ID不能为空")
    private Long dishId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "1.00", message = "评分不能低于1分")
    @DecimalMax(value = "5.00", message = "评分不能高于5分")
    private BigDecimal rating;
}
