package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.dto.LoginRequest;
import com.example.shitang.dto.RegisterRequest;
import com.example.shitang.service.UserService;
import com.example.shitang.vo.LoginResponse;
import com.example.shitang.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @GetMapping("/userinfo")
    public Result<UserInfoVO> userinfo() {
        return Result.success(userService.currentUser());
    }
}
