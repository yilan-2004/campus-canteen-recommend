package com.example.shitang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("canteen_window")
public class CanteenWindow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long canteenId;
    private Long merchantId;
    private String name;
    private String floor;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
