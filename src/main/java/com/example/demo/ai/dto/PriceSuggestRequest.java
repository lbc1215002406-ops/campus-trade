package com.example.demo.ai.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceSuggestRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer itemCondition;
    private String categoryName;
}
