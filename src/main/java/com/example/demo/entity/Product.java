package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String images;
    private Integer itemCondition;
    private Integer status;
    private Integer isPinned;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long buyerId;
    private LocalDateTime soldTime;
    @TableField(exist = false)
    private String sellerName;
}
