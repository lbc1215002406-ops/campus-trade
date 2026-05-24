package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Favorite;
import com.example.demo.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private HttpServletRequest request;

    private Long currentUserId() {
        return (Long) request.getAttribute("userId");
    }

    @PostMapping("/{productId}")
    public Result<String> add(@PathVariable Long productId) {
        Long userId = currentUserId();
        if (favoriteService.isFavorited(userId, productId)) {
            return Result.error("已收藏过该商品");
        }
        favoriteService.add(userId, productId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/{productId}")
    public Result<String> remove(@PathVariable Long productId) {
        favoriteService.remove(currentUserId(), productId);
        return Result.success("取消收藏");
    }

    @GetMapping
    public Result<IPage<Favorite>> myFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Favorite> p = new Page<>(page, pageSize);
        return Result.success(favoriteService.pageByUserId(p, currentUserId()));
    }

    @GetMapping("/check/{productId}")
    public Result<Boolean> check(@PathVariable Long productId) {
        return Result.success(favoriteService.isFavorited(currentUserId(), productId));
    }
}
