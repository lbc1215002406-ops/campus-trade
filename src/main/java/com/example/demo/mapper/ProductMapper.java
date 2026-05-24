package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("<script>SELECT p.*, u.nickname AS seller_name FROM product p " +
            "LEFT JOIN user u ON p.user_id = u.id " +
            "WHERE p.status = 1 " +
            "<if test='keyword != null'>AND p.title LIKE CONCAT('%', #{keyword}, '%')</if> " +
            "<if test='categoryId != null'>AND p.category_id = #{categoryId}</if> " +
            "ORDER BY p.is_pinned DESC, ${orderBy}</script>")
    IPage<Product> selectPageWithSeller(Page<Product> page,
                                        @Param("keyword") String keyword,
                                        @Param("categoryId") Long categoryId,
                                        @Param("orderBy") String orderBy);

    @Select("SELECT AVG(price) FROM product WHERE category_id = #{categoryId} AND status = 1")
    BigDecimal selectAvgPriceByCategory(@Param("categoryId") Long categoryId);

    @Select("<script>SELECT p.*, u.nickname AS seller_name FROM product p " +
            "LEFT JOIN user u ON p.user_id = u.id WHERE 1=1 " +
            "<if test='keyword != null'>AND p.title LIKE CONCAT('%', #{keyword}, '%')</if> " +
            "ORDER BY p.is_pinned DESC, p.create_time DESC</script>")
    IPage<Product> selectAdminPage(Page<Product> page, @Param("keyword") String keyword);

    @Select("SELECT * FROM product WHERE id = #{id} FOR UPDATE")
    Product selectByIdForUpdate(@Param("id") Long id);
}
