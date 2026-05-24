package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping
    public Result<IPage<Product>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sortBy) {
        Page<Product> p = new Page<>(page, pageSize);
        return Result.success(productService.pageWithSeller(p, keyword, categoryId, sortBy));
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<String> save(@RequestBody Product product) {
        Long userId = (Long) request.getAttribute("userId");
        product.setUserId(userId);
        productService.save(product);
        return Result.success("发布成功");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productService.updateById(product);
        return Result.success("修改成功");
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        productService.updateStatus(id, body.get("status"));
        return Result.success("操作成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/my")
    public Result<IPage<Product>> myProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Product> p = new Page<>(page, pageSize);
        return Result.success(productService.pageByUserId(p, userId));
    }

    @GetMapping("/admin/all")
    public Result<IPage<Product>> adminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Product> p = new Page<>(page, pageSize);
        return Result.success(productService.adminPage(p, keyword));
    }

    @PutMapping("/admin/{id}/toggle-pin")
    public Result<String> togglePin(@PathVariable Long id) {
        productService.togglePin(id);
        return Result.success("操作成功");
    }
}
