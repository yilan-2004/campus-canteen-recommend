package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.common.BizException;
import com.example.shitang.dto.WindowRequest;
import com.example.shitang.entity.CanteenWindow;
import com.example.shitang.entity.User;
import com.example.shitang.mapper.CanteenWindowMapper;
import com.example.shitang.service.CanteenService;
import com.example.shitang.service.CanteenWindowService;
import com.example.shitang.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CanteenWindowServiceImpl extends ServiceImpl<CanteenWindowMapper, CanteenWindow> implements CanteenWindowService {
    private final CanteenService canteenService;
    private final UserService userService;

    @Override
    public List<CanteenWindow> listWindows(Long canteenId, Long merchantId, String keyword, Integer status) {
        return list(new LambdaQueryWrapper<CanteenWindow>()
                .eq(canteenId != null, CanteenWindow::getCanteenId, canteenId)
                .eq(merchantId != null, CanteenWindow::getMerchantId, merchantId)
                .like(StringUtils.hasText(keyword), CanteenWindow::getName, keyword)
                .eq(status != null, CanteenWindow::getStatus, status)
                .orderByDesc(CanteenWindow::getCreateTime));
    }

    @Override
    public CanteenWindow createWindow(WindowRequest request) {
        validateRefs(request.getCanteenId(), request.getMerchantId());
        LocalDateTime now = LocalDateTime.now();
        CanteenWindow window = new CanteenWindow();
        window.setCanteenId(request.getCanteenId());
        window.setMerchantId(request.getMerchantId());
        window.setName(request.getName());
        window.setFloor(request.getFloor());
        window.setDescription(request.getDescription());
        window.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        window.setCreateTime(now);
        window.setUpdateTime(now);
        save(window);
        return window;
    }

    @Override
    public CanteenWindow updateWindow(Long id, WindowRequest request) {
        CanteenWindow window = requireWindow(id);
        validateRefs(request.getCanteenId(), request.getMerchantId());
        window.setCanteenId(request.getCanteenId());
        window.setMerchantId(request.getMerchantId());
        window.setName(request.getName());
        window.setFloor(request.getFloor());
        window.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            window.setStatus(request.getStatus());
        }
        window.setUpdateTime(LocalDateTime.now());
        updateById(window);
        return window;
    }

    @Override
    public void disableWindow(Long id) {
        CanteenWindow window = requireWindow(id);
        window.setStatus(0);
        window.setUpdateTime(LocalDateTime.now());
        updateById(window);
    }

    private void validateRefs(Long canteenId, Long merchantId) {
        if (canteenService.getById(canteenId) == null) {
            throw new BizException("所属食堂不存在");
        }
        if (merchantId != null) {
            User merchant = userService.getById(merchantId);
            if (merchant == null) {
                throw new BizException("商户用户不存在");
            }
        }
    }

    private CanteenWindow requireWindow(Long id) {
        CanteenWindow window = getById(id);
        if (window == null) {
            throw new BizException("食堂窗口不存在");
        }
        return window;
    }
}
