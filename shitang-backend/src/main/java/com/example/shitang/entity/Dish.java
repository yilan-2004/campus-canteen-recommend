package com.example.shitang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long windowId;
    private Long categoryId;
    private String name;
    private String image;
    private BigDecimal price;
    private String description;
    private String taste;
    private Integer calories;
    private BigDecimal avgRating;
    private Integer salesCount;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
