package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT o.*, p.title AS product_title, p.images AS product_images, p.price AS product_price, " +
            "ub.nickname AS buyer_name, us.nickname AS seller_name " +
            "FROM orders o " +
            "LEFT JOIN product p ON o.product_id = p.id " +
            "LEFT JOIN user ub ON o.buyer_id = ub.id " +
            "LEFT JOIN user us ON o.seller_id = us.id " +
            "WHERE o.buyer_id = #{userId} OR o.seller_id = #{userId} " +
            "ORDER BY o.create_time DESC")
    IPage<Order> selectPageByUser(Page<Order> page, @Param("userId") Long userId);
}
