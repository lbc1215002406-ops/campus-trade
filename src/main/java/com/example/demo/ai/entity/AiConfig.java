package com.example.demo.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_config")
public class AiConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String provider;
    private String apiKey;
    private String modelName;
    private String baseUrl;
    private Integer isActive;
    private Integer maxTokens;
    private BigDecimal temperature;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
