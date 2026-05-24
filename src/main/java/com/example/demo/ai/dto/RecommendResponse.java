package com.example.demo.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendResponse {
    private List<RecommendItem> recommendations;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecommendItem {
        private Long productId;
        private String reason;
    }
}
