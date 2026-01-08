package com.tripmate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("attractions")
public class Attraction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String cover;
    private String description;
    private String location;
    private BigDecimal rating;
    private String tags;
    private String openTime; // 开放时间
    private String ticketPrice; // 门票价格
    private String suggestedDuration; // 建议游玩时长
    private Integer likesCount;
    private Integer favoritesCount;
    private Integer commentsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
