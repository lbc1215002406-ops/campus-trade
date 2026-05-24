package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Order;

public interface OrderService {
    Order getById(Long id);
    void createOrder(Order order);
    void confirmOrder(Long orderId, Long userId);
    void completeOrder(Long orderId, Long userId);
    void cancelOrder(Long orderId, Long userId, String reason);
    IPage<Order> pageByUser(Page<Order> page, Long userId);
}
