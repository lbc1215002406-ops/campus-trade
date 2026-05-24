package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.OrderMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMessageMapper extends BaseMapper<OrderMessage> {

    @Select("SELECT m.*, u.nickname AS sender_name FROM order_message m " +
            "LEFT JOIN user u ON m.sender_id = u.id " +
            "WHERE m.order_id = #{orderId} ORDER BY m.create_time ASC")
    List<OrderMessage> selectByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT COUNT(*) FROM order_message WHERE order_id = #{orderId} AND sender_id != #{userId} AND is_read = 0")
    int countUnread(@Param("orderId") Long orderId, @Param("userId") Long userId);

    @Update("UPDATE order_message SET is_read = 1 WHERE order_id = #{orderId} AND sender_id != #{userId} AND is_read = 0")
    void markAsRead(@Param("orderId") Long orderId, @Param("userId") Long userId);

    @Select("<script>SELECT order_id AS orderId, COUNT(*) AS cnt FROM order_message " +
            "WHERE order_id IN <foreach collection='orderIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND sender_id != #{userId} AND is_read = 0 GROUP BY order_id</script>")
    List<Map<String, Object>> batchCountUnread(@Param("orderIds") List<Long> orderIds, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM order_message om " +
            "JOIN orders o ON om.order_id = o.id " +
            "WHERE (o.buyer_id = #{userId} OR o.seller_id = #{userId}) " +
            "AND om.sender_id != #{userId} AND om.is_read = 0 AND o.status IN (0, 1)")
    int countTotalUnread(@Param("userId") Long userId);
}
