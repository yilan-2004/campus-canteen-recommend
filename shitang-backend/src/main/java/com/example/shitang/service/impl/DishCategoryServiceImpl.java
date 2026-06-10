package com.example.shitang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shitang.common.BizException;
import com.example.shitang.dto.CategoryRequest;
import com.example.shitang.entity.DishCategory;
import com.example.shitang.mapper.DishCategoryMapper;
import com.example.shitang.service.DishCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DishCategoryServiceImpl extends ServiceImpl<DishCategoryMapper, DishCategory> implements DishCategoryService {

    @Override
    public List<DishCategory> listCategories(String keyword, Integer status) {
        return list(new LambdaQueryWrapper<DishCategory>()
                .like(StringUtils.hasText(keyword), DishCategory::getName, keyword)
                .eq(status != null, DishCategory::getStatus, status)
                .orderByAsc(DishCategory::getSort)
                .orderByDesc(DishCategory::getCreateTime));
    }

    @Override
    public DishCategory createCategory(CategoryRequest request) {
        LocalDateTime now = LocalDateTime.now();
        DishCategory category = new DishCategory();
        category.setName(request.getName());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        category.setCreateTime(now);
        category.setUpdateTime(now);
        save(category);
        return category;
    }

    @Override
    public DishCategory updateCategory(Long id, CategoryRequest request) {
        DishCategory category = requireCategory(id);
        category.setName(request.getName());
        if (request.getSort() != null) {
            category.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
        return category;
    }

    @Override
    public void disableCategory(Long id) {
        DishCategory category = requireCategory(id);
        category.setStatus(0);
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
    }

    private DishCategory requireCategory(Long id) {
        DishCategory category = getById(id);
        if (category == null) {
            throw new BizException("菜品分类不存在");
        }
        return category;
    }
}
