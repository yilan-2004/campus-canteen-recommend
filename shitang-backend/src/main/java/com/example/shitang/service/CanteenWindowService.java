package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.dto.WindowRequest;
import com.example.shitang.entity.CanteenWindow;

import java.util.List;

public interface CanteenWindowService extends IService<CanteenWindow> {
    List<CanteenWindow> listWindows(Long canteenId, Long merchantId, String keyword, Integer status);
    CanteenWindow createWindow(WindowRequest request);
    CanteenWindow updateWindow(Long id, WindowRequest request);
    void disableWindow(Long id);
}
