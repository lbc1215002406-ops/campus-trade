package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime completeTime;
    private String cancelReason;

    @TableField(exist = false)
    private String productTitle;
    @TableField(exist = false)
    private String productImages;
    @TableField(exist = false)
    private java.math.BigDecimal productPrice;
    @TableField(exist = false)
    private String buyerName;
    @TableField(exist = false)
    private String sellerName;
}
