package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    @Transactional
    public void createOrder(Order order) {
        Product product = productMapper.selectByIdForUpdate(order.getProductId());
        if (product == null) throw new RuntimeException("商品不存在");
        if (product.getStatus() != 1) throw new RuntimeException("商品已售出或已下架");
        if (product.getUserId().equals(order.getBuyerId())) throw new RuntimeException("不能购买自己的商品");

        // 检查该商品是否已有进行中的订单（状态0或1），FOR UPDATE锁下安全
        Long activeCount = orderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getProductId, order.getProductId())
                        .in(Order::getStatus, 0, 1));
        if (activeCount != null && activeCount > 0)
            throw new RuntimeException("该商品已被其他用户抢先下单");

        order.setSellerId(product.getUserId());
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getSellerId().equals(userId)) throw new RuntimeException("只有卖家可以确认订单");
        if (order.getStatus() != 0) throw new RuntimeException("当前订单状态不允许此操作");

        order.setStatus(1);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId))
            throw new RuntimeException("无权操作此订单");
        if (order.getStatus() != 1) throw new RuntimeException("当前订单状态不允许此操作");

        order.setStatus(2);
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        Product product = productMapper.selectByIdForUpdate(order.getProductId());
        if (product != null && product.getStatus() == 1) {
            product.setStatus(2);
            product.setBuyerId(order.getBuyerId());
            product.setSoldTime(LocalDateTime.now());
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId))
            throw new RuntimeException("无权操作此订单");
        if (order.getStatus() == 2) throw new RuntimeException("已完成的订单不能取消");

        order.setStatus(3);
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public IPage<Order> pageByUser(Page<Order> page, Long userId) {
        return orderMapper.selectPageByUser(page, userId);
    }

    @Scheduled(fixedRate = 600000)
    @Transactional
    public void autoCancelStaleOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(24);
        List<Order> staleOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 0)
                        .lt(Order::getCreateTime, deadline));
        for (Order order : staleOrders) {
            order.setStatus(3);
            order.setCancelReason("24小时内卖家未确认，系统自动取消");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }
    }
}
