package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    int countByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND product_id = #{productId}")
    Favorite selectByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}
