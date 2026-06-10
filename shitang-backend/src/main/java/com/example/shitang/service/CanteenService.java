package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.dto.CanteenRequest;
import com.example.shitang.entity.Canteen;

import java.util.List;

public interface CanteenService extends IService<Canteen> {
    List<Canteen> listCanteens(String keyword, Integer status);
    Canteen createCanteen(CanteenRequest request);
    Canteen updateCanteen(Long id, CanteenRequest request);
    void disableCanteen(Long id);
}
