package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.dto.LoginRequest;
import com.example.shitang.dto.RegisterRequest;
import com.example.shitang.entity.User;
import com.example.shitang.vo.LoginResponse;
import com.example.shitang.vo.UserInfoVO;

import java.util.List;

public interface UserService extends IService<User> {
    UserInfoVO register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserInfoVO currentUser();

    User currentUserEntity();

    List<UserInfoVO> listUsers(String keyword, String role, Integer status);

    UserInfoVO updateStatus(Long id, Integer status);

    UserInfoVO updateRole(Long id, String role);

    UserInfoVO updateProfile(Long id, String realName, String college, String grade, String tastePreference);

    void changePassword(Long id, String oldPassword, String newPassword);
}
