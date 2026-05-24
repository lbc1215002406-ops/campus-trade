package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Product;
import java.util.List;

public interface ProductService {
    IPage<Product> pageWithSeller(Page<Product> page, String keyword, Long categoryId, String sortBy);
    Product getById(Long id);
    void save(Product product);
    void updateById(Product product);
    void updateStatus(Long id, Integer status);
    void removeById(Long id);
    IPage<Product> pageByUserId(Page<Product> page, Long userId);
    IPage<Product> adminPage(Page<Product> page, String keyword);
    void togglePin(Long id);
    List<Product> listAll();
}
