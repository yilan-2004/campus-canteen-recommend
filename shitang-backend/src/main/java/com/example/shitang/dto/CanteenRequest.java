package com.example.shitang.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CanteenRequest {
    @NotBlank(message = "食堂名称不能为空")
    private String name;

    @NotBlank(message = "食堂位置不能为空")
    private String location;

    private String openTime;
    private String description;
    private Integer status;
}
