package com.example.shitang.controller;

import com.example.shitang.common.Result;
import com.example.shitang.service.FavoriteService;
import com.example.shitang.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/{dishId}")
    public Result<FavoriteVO> add(@PathVariable Long dishId) {
        return Result.success(favoriteService.addFavorite(dishId));
    }

    @DeleteMapping("/{dishId}")
    public Result<Void> remove(@PathVariable Long dishId) {
        favoriteService.removeFavorite(dishId);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<FavoriteVO>> my() {
        return Result.success(favoriteService.listMine());
    }
}
