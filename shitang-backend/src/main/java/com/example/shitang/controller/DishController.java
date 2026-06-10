package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.dto.DishRequest;
import com.example.shitang.dto.DishTagRequest;
import com.example.shitang.service.DishService;
import com.example.shitang.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "菜品管理")
@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;

    @GetMapping
    @Operation(summary = "菜品列表(支持多条件筛选)")
    public Result<List<DishVO>> list(@Parameter(description = "关键字(菜名)") @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Long canteenId,
                                     @RequestParam(required = false) Long windowId,
                                     @RequestParam(required = false) Long categoryId,
                                     @RequestParam(required = false) Long tagId,
                                     @RequestParam(required = false) String taste,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(required = false) BigDecimal minPrice,
                                     @RequestParam(required = false) BigDecimal maxPrice) {
        return Result.success(dishService.listDishes(keyword, canteenId, windowId, categoryId, tagId, taste, status, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    public Result<DishVO> detail(@PathVariable Long id) {
        return Result.success(dishService.viewDishDetail(id));
    }

    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id) {
        return Result.success(dishService.likeDish(id));
    }

    @PostMapping
    public Result<DishVO> create(@Valid @RequestBody DishRequest request) {
        return Result.success(dishService.createDish(request));
    }

    @PutMapping("/{id}")
    public Result<DishVO> update(@PathVariable Long id, @Valid @RequestBody DishRequest request) {
        return Result.success(dishService.updateDish(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dishService.disableDish(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<DishVO> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.success(dishService.updateStatus(id, status));
    }

    @PutMapping("/{id}/tags")
    public Result<DishVO> updateTags(@PathVariable Long id, @RequestBody DishTagRequest request) {
        return Result.success(dishService.updateTags(id, request.getTagIds()));
    }
}
