package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("order_message")
public class OrderMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long senderId;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String senderName;
}
