package com.tripmate.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String email;
    private String avatar;
    private String bio;  // 个人简介
}

