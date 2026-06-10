package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.dto.BehaviorRequest;
import com.example.shitang.entity.UserBehavior;
import com.example.shitang.vo.BehaviorVO;

import java.math.BigDecimal;
import java.util.List;

public interface BehaviorService extends IService<UserBehavior> {
    UserBehavior record(Long userId, Long dishId, String behaviorType, BigDecimal behaviorWeight);
    UserBehavior recordCurrentUser(BehaviorRequest request);
    List<BehaviorVO> listByUser(Long userId);
    List<BehaviorVO> listMine();
    boolean hasBehavior(Long userId, Long dishId, String behaviorType);
    void removeBehavior(Long userId, Long dishId, String behaviorType);
}
