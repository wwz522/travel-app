package com.tripmate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("routes")
public class Route {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String cover;
    private String description;
    private Integer duration;
    private String budget;
    private String tags;
    private String tips;
    private Integer likesCount;
    private Integer favoritesCount;
    private Integer commentsCount;
    private Boolean isDraft;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
