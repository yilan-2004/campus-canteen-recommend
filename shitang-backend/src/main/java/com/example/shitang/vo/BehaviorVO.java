package com.example.shitang.vo;

import com.example.shitang.entity.UserBehavior;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BehaviorVO {
    private Long id;
    private Long userId;
    private Long dishId;
    private String dishName;
    private String behaviorType;
    private BigDecimal behaviorWeight;
    private LocalDateTime createTime;

    public static BehaviorVO from(UserBehavior behavior) {
        BehaviorVO vo = new BehaviorVO();
        vo.setId(behavior.getId());
        vo.setUserId(behavior.getUserId());
        vo.setDishId(behavior.getDishId());
        vo.setBehaviorType(behavior.getBehaviorType());
        vo.setBehaviorWeight(behavior.getBehaviorWeight());
        vo.setCreateTime(behavior.getCreateTime());
        return vo;
    }
}
