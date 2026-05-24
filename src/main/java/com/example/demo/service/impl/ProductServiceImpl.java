package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Product;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public IPage<Product> pageWithSeller(Page<Product> page, String keyword, Long categoryId, String sortBy) {
        String orderBy = resolveOrderBy(sortBy);
        return productMapper.selectPageWithSeller(page, keyword, categoryId, orderBy);
    }

    private String resolveOrderBy(String sortBy) {
        if ("price_asc".equals(sortBy)) return "p.price ASC";
        if ("price_desc".equals(sortBy)) return "p.price DESC";
        if ("popularity".equals(sortBy)) return "p.view_count DESC, p.create_time DESC";
        return "p.create_time DESC";
    }

    @Override
    public Product getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            product.setViewCount(product.getViewCount() == null ? 1 : product.getViewCount() + 1);
            productMapper.updateById(product);
        }
        return product;
    }

    @Override
    public void save(Product product) {
        product.setStatus(1);
        product.setViewCount(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
    }

    @Override
    public void updateById(Product product) {
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    public void removeById(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public IPage<Product> pageByUserId(Page<Product> page, Long userId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getUserId, userId).orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Product> adminPage(Page<Product> page, String keyword) {
        return productMapper.selectAdminPage(page, keyword);
    }

    @Override
    public void togglePin(Long id) {
        Product product = productMapper.selectById(id);
        if (product != null) {
            product.setIsPinned(product.getIsPinned() != null && product.getIsPinned() == 1 ? 0 : 1);
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);
        }
    }

    @Override
    public List<Product> listAll() {
        return productMapper.selectList(null);
    }
}
