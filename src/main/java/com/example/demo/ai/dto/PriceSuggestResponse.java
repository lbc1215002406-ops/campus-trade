package com.example.demo.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceSuggestResponse {
    private BigDecimal suggestedPrice;
    private PriceRange priceRange;
    private String reasoning;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PriceRange {
        private BigDecimal min;
        private BigDecimal max;
    }
}
