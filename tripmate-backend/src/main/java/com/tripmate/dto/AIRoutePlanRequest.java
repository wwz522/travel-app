package com.tripmate.dto;

import lombok.Data;

@Data
public class AIRoutePlanRequest {
    private String origin;           // 出发地
    private String destination;      // 目的地
    private Integer days;            // 行程天数
    private String budget;           // 预算范围（如：1000-2000）
    private String travelMode;       // 交通方式（high-speed-rail/train/flight/self-drive/bus）
    private String stayLevel;        // 住宿档次（budget/comfort/luxury）
    private String preferences;      // 偏好（如：美食、文化、自然风光等）
}

