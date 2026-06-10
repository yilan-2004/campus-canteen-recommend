package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.service.UserService;
import com.example.shitang.vo.UserInfoVO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Result<List<UserInfoVO>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String role,
                                         @RequestParam(required = false) Integer status) {
        return Result.success(userService.listUsers(keyword, role, status));
    }

    @PutMapping("/{id}/status")
    public Result<UserInfoVO> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(userService.updateStatus(id, status));
    }

    @PutMapping("/{id}/role")
    public Result<UserInfoVO> updateRole(@PathVariable Long id, @RequestParam String role) {
        return Result.success(userService.updateRole(id, role));
    }

    @PutMapping("/profile")
    public Result<UserInfoVO> updateProfile(@RequestBody ProfileRequest request) {
        Long userId = userService.currentUserEntity().getId();
        return Result.success(userService.updateProfile(userId, request.getRealName(), request.getCollege(), request.getGrade(), request.getTastePreference()));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordRequest request) {
        Long userId = userService.currentUserEntity().getId();
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    @Data
    public static class ProfileRequest {
        private String realName;
        private String college;
        private String grade;
        private String tastePreference;
    }

    @Data
    public static class PasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}
