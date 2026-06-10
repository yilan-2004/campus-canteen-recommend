package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.dto.CategoryRequest;
import com.example.shitang.entity.DishCategory;

import java.util.List;

public interface DishCategoryService extends IService<DishCategory> {
    List<DishCategory> listCategories(String keyword, Integer status);
    DishCategory createCategory(CategoryRequest request);
    DishCategory updateCategory(Long id, CategoryRequest request);
    void disableCategory(Long id);
}
