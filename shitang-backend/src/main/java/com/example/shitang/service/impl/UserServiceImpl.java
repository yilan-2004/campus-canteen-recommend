package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.common.BizException;
import com.example.shitang.common.ResultCode;
import com.example.shitang.dto.LoginRequest;
import com.example.shitang.dto.RegisterRequest;
import com.example.shitang.entity.User;
import com.example.shitang.mapper.UserMapper;
import com.example.shitang.security.LoginUser;
import com.example.shitang.service.UserService;
import com.example.shitang.utils.JwtUtils;
import com.example.shitang.vo.LoginResponse;
import com.example.shitang.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public UserInfoVO register(RegisterRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new BizException("用户名已存在");
        }
        if (existsByStudentNo(request.getStudentNo())) {
            throw new BizException("学号已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("STUDENT");
        user.setStudentNo(request.getStudentNo());
        user.setRealName(request.getRealName());
        user.setCollege(request.getCollege());
        user.setGrade(request.getGrade());
        user.setTastePreference(request.getTastePreference());
        user.setStatus(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        save(user);
        return UserInfoVO.from(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        String token = jwtUtils.generateToken(user);
        return new LoginResponse(token, "Bearer", UserInfoVO.from(user));
    }

    @Override
    public UserInfoVO currentUser() {
        return UserInfoVO.from(currentUserEntity());
    }

    @Override
    public User currentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return loginUser.getUser();
    }

    private boolean existsByUsername(String username) {
        return count(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    private boolean existsByStudentNo(String studentNo) {
        return count(new LambdaQueryWrapper<User>().eq(User::getStudentNo, studentNo)) > 0;
    }

    @Override
    public List<UserInfoVO> listUsers(String keyword, String role, Integer status) {
        return list(new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(keyword), User::getUsername, keyword)
                .or().like(StringUtils.hasText(keyword), User::getRealName, keyword)
                .eq(StringUtils.hasText(role), User::getRole, role)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreateTime))
                .stream().map(UserInfoVO::from).toList();
    }

    @Override
    public UserInfoVO updateStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) throw new BizException("用户不存在");
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
        return UserInfoVO.from(user);
    }

    @Override
    public UserInfoVO updateRole(Long id, String role) {
        if (!"STUDENT".equals(role) && !"MERCHANT".equals(role) && !"ADMIN".equals(role)) {
            throw new BizException("角色只能是 STUDENT、MERCHANT 或 ADMIN");
        }
        User user = getById(id);
        if (user == null) throw new BizException("用户不存在");
        user.setRole(role);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
        return UserInfoVO.from(user);
    }

    @Override
    public UserInfoVO updateProfile(Long id, String realName, String college, String grade, String tastePreference) {
        User user = getById(id);
        if (user == null) throw new BizException("用户不存在");
        if (StringUtils.hasText(realName)) user.setRealName(realName);
        if (college != null) user.setCollege(college);
        if (grade != null) user.setGrade(grade);
        if (tastePreference != null) user.setTastePreference(tastePreference);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
        return UserInfoVO.from(user);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = getById(id);
        if (user == null) throw new BizException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码错误");
        }
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BizException("新密码长度不能少于6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }
}
