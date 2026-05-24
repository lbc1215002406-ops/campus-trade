package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderMessage;
import com.example.demo.mapper.OrderMessageMapper;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMessageMapper orderMessageMapper;

    @Autowired
    private HttpServletRequest request;

    private Long currentUserId() {
        return (Long) request.getAttribute("userId");
    }

    @GetMapping
    public Result<IPage<Order>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Order> p = new Page<>(page, pageSize);
        return Result.success(orderService.pageByUser(p, currentUserId()));
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @PostMapping
    public Result<String> create(@RequestBody Order order) {
        order.setBuyerId(currentUserId());
        try {
            orderService.createOrder(order);
            return Result.success("下单成功，请等待卖家确认");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/confirm")
    public Result<String> confirm(@PathVariable Long id) {
        try {
            orderService.confirmOrder(id, currentUserId());
            return Result.success("已确认，请与买家沟通交易地点");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/complete")
    public Result<String> complete(@PathVariable Long id) {
        try {
            orderService.completeOrder(id, currentUserId());
            return Result.success("交易完成");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body != null ? body.get("reason") : null;
            orderService.cancelOrder(id, currentUserId(), reason);
            return Result.success("订单已取消");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{orderId}/messages")
    public Result<List<OrderMessage>> getMessages(@PathVariable Long orderId) {
        orderMessageMapper.markAsRead(orderId, currentUserId());
        return Result.success(orderMessageMapper.selectByOrderId(orderId));
    }

    @PostMapping("/{orderId}/messages")
    public Result<OrderMessage> sendMessage(@PathVariable Long orderId,
                                            @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.isBlank()) return Result.error("消息不能为空");
        OrderMessage msg = new OrderMessage();
        msg.setOrderId(orderId);
        msg.setSenderId(currentUserId());
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        orderMessageMapper.insert(msg);
        return Result.success(msg);
    }

    @GetMapping("/messages/unread/batch")
    public Result<Map<Long, Integer>> getBatchUnread(@RequestParam List<Long> orderIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (orderIds == null || orderIds.isEmpty()) return Result.success(result);
        List<Map<String, Object>> rows = orderMessageMapper.batchCountUnread(orderIds, currentUserId());
        for (Map<String, Object> row : rows) {
            Long orderId = ((Number) row.get("orderId")).longValue();
            int cnt = ((Number) row.get("cnt")).intValue();
            result.put(orderId, cnt);
        }
        return Result.success(result);
    }

    @GetMapping("/messages/unread/total")
    public Result<Map<String, Object>> getTotalUnread() {
        int total = orderMessageMapper.countTotalUnread(currentUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        return Result.success(result);
    }

    @GetMapping("/{orderId}/messages/unread")
    public Result<Map<String, Object>> getUnread(@PathVariable Long orderId) {
        int count = orderMessageMapper.countUnread(orderId, currentUserId());
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }
}
