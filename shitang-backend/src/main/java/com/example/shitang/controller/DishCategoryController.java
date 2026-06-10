package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.dto.CategoryRequest;
import com.example.shitang.entity.DishCategory;
import com.example.shitang.service.DishCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class DishCategoryController {
    private final DishCategoryService categoryService;

    @GetMapping
    public Result<List<DishCategory>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status) {
        return Result.success(categoryService.listCategories(keyword, status));
    }

    @GetMapping("/{id}")
    public Result<DishCategory> detail(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PostMapping
    public Result<DishCategory> create(@Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public Result<DishCategory> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.disableCategory(id);
        return Result.success();
    }
}
