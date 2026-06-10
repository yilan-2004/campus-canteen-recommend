package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.dto.DishRequest;
import com.example.shitang.entity.Dish;
import com.example.shitang.vo.DishVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DishService extends IService<Dish> {
    List<DishVO> listDishes(String keyword, Long canteenId, Long windowId, Long categoryId, Long tagId,
                            String taste, Integer status, BigDecimal minPrice, BigDecimal maxPrice);
    DishVO getDishDetail(Long id);
    DishVO viewDishDetail(Long id);
    Map<String, Object> likeDish(Long id);
    DishVO createDish(DishRequest request);
    DishVO updateDish(Long id, DishRequest request);
    void disableDish(Long id);
    DishVO updateStatus(Long id, Integer status);
    DishVO updateTags(Long id, List<Long> tagIds);
}
