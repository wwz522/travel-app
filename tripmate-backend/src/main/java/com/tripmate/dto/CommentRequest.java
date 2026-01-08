package com.tripmate.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CommentRequest {
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
