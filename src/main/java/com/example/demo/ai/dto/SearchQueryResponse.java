package com.example.demo.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchQueryResponse {
    private List<Long> categoryIds;
    private List<String> keywords;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String sortBy;
    private String parsedQuery;
}
