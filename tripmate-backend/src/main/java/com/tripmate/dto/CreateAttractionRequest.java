package com.tripmate.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateAttractionRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100")
    private String title;

    private String cover;

    @NotBlank(message = "描述不能为空")
    private String description;

    private String location;

    @NotNull(message = "评分不能为空")
    private BigDecimal rating;

    private List<String> tags;
    
    private String openTime;
    
    private String ticketPrice;
    
    private String suggestedDuration;
}
