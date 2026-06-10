package com.example.shitang.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {
    @NotBlank(message = "标签名称不能为空")
    private String name;

    @NotBlank(message = "标签类型不能为空")
    private String type;
}
