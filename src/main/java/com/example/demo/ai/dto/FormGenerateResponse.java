package com.example.demo.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormGenerateResponse {
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String categoryName;
    private Integer itemCondition;
    private List<String> imageSuggestions;
}
