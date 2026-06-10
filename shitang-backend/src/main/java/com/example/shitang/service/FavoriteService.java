package com.example.shitang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shitang.entity.Favorite;
import com.example.shitang.vo.FavoriteVO;

import java.util.List;

public interface FavoriteService extends IService<Favorite> {
    FavoriteVO addFavorite(Long dishId);
    void removeFavorite(Long dishId);
    List<FavoriteVO> listMine();
    boolean hasFavorite(Long userId, Long dishId);
}
