package com.tripmate.dto;

import lombok.Data;

@Data
public class AIRoutePlanResponse {
    private String title;            // 路线标题
    private String description;     // 路线描述
    private String itinerary;       // 详细行程（包含每天的时间安排、景点、餐饮、交通）
    private String tips;            // 出行提示
    private String budgetBreakdown; // 预算分解（包含各项费用明细）
    private String dailySchedule;  // 每日详细时间表（JSON格式）
}

