package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.dto.BehaviorRequest;
import com.example.shitang.entity.UserBehavior;
import com.example.shitang.service.BehaviorService;
import com.example.shitang.vo.BehaviorVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/behavior")
@RequiredArgsConstructor
public class BehaviorController {
    private final BehaviorService behaviorService;

    @PostMapping
    public Result<UserBehavior> record(@Valid @RequestBody BehaviorRequest request) {
        return Result.success(behaviorService.recordCurrentUser(request));
    }

    @GetMapping("/user/{userId}")
    public Result<List<BehaviorVO>> userBehaviors(@PathVariable Long userId) {
        return Result.success(behaviorService.listByUser(userId));
    }

    @GetMapping("/my")
    public Result<List<BehaviorVO>> myBehaviors() {
        return Result.success(behaviorService.listMine());
    }
}
