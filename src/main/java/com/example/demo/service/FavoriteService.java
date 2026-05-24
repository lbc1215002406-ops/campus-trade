package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Favorite;

public interface FavoriteService {
    void add(Long userId, Long productId);
    void remove(Long userId, Long productId);
    IPage<Favorite> pageByUserId(Page<Favorite> page, Long userId);
    boolean isFavorited(Long userId, Long productId);
}
