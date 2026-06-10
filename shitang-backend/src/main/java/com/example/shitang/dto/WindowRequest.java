package com.example.shitang.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WindowRequest {
    @NotNull(message = "所属食堂不能为空")
    private Long canteenId;

    private Long merchantId;

    @NotBlank(message = "窗口名称不能为空")
    private String name;

    private String floor;
    private String description;
    private Integer status;
}
