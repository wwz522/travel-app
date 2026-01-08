package com.tripmate.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class CreateRouteRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100")
    private String title;

    private String cover;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotNull(message = "行程天数不能为空")
    private Integer duration;

    private String budget;

    private List<String> tags;
    
    private String tips;
    
    private Boolean isDraft;  // 是否为草稿
}
