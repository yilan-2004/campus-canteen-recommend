package com.example.shitang.dto;

import lombok.Data;

import java.util.List;

@Data
public class DishTagRequest {
    private List<Long> tagIds;
}
