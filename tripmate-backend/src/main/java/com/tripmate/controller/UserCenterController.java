package com.tripmate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tripmate.annotation.RequireAuth;
import com.tripmate.common.Result;
import com.tripmate.dto.ContentItem;
import com.tripmate.service.UserCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserCenterController {

    @Autowired
    private UserCenterService userCenterService;

    @GetMapping("/posts")
    @RequireAuth
    public Result<IPage<ContentItem>> getMyPosts(
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        IPage<ContentItem> result = userCenterService.getUserPosts(userId, type, page, size);
        return Result.success(result);
    }

    @GetMapping("/favorites")
    @RequireAuth
    public Result<IPage<ContentItem>> getMyFavorites(
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        IPage<ContentItem> result = userCenterService.getUserFavorites(userId, type, page, size);
        return Result.success(result);
    }

    @DeleteMapping("/post/{id}")
    @RequireAuth
    public Result<?> deletePost(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userCenterService.deletePost(id, userId);
        return Result.success("删除成功");
    }

    @DeleteMapping("/favorite/{id}")
    @RequireAuth
    public Result<?> removeFavorite(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userCenterService.removeFavorite(id, userId);
        return Result.success("取消收藏成功");
    }

    @GetMapping("/me")
    @RequireAuth
    public Result<?> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        com.tripmate.entity.User user = userCenterService.getUserProfile(userId);
        return Result.success(user);
    }

    @PutMapping("/profile")
    @RequireAuth
    public Result<?> updateProfile(
            @RequestBody com.tripmate.dto.UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userCenterService.updateProfile(userId, request);
        return Result.success("更新成功");
    }
}
