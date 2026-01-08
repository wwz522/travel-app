package com.tripmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentItem {
    private Long id;
    private Long userId;  // 作者ID，用于权限判断
    private String type;
    private String title;
    private String cover;
    private String author;
    private String avatar;
    private Integer likes;
    private Integer favorites;
    private Integer comments;
    private List<String> tags;
    private String description;
    private BigDecimal rating;
    private String location;
    private Integer duration;
    private String budget;
    private String openTime; // 开放时间（景点）
    private String ticketPrice; // 门票价格（景点）
    private String suggestedDuration; // 建议游玩时长（景点）
    private String tips; // 出行提示（路线）
    private Boolean isDraft; // 是否为草稿（路线）
    private LocalDateTime createdAt;
}
