package com.tripmate.controller;

import com.tripmate.annotation.RequireAuth;
import com.tripmate.common.Result;
import com.tripmate.dto.CommentRequest;
import com.tripmate.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/interaction")
public class InteractionController {

    @Autowired
    private InteractionService interactionService;

    @PostMapping("/like")
    @RequireAuth
    public Result<?> toggleLike(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        interactionService.toggleLike(targetType, targetId, userId);
        return Result.success("操作成功");
    }

    @PostMapping("/favorite")
    @RequireAuth
    public Result<?> toggleFavorite(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        interactionService.toggleFavorite(targetType, targetId, userId);
        return Result.success("操作成功");
    }

    @PostMapping("/comment")
    @RequireAuth
    public Result<Long> addComment(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long commentId = interactionService.addComment(targetType, targetId, request, userId);
        return Result.success(commentId);
    }

    @GetMapping("/comments")
    public Result<?> getComments(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return Result.success(interactionService.getComments(targetType, targetId));
    }

    @DeleteMapping("/comment/{id}")
    @RequireAuth
    public Result<?> deleteComment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        interactionService.deleteComment(id, userId);
        return Result.success("删除成功");
    }
    
    @PostMapping("/comment/{id}/like")
    @RequireAuth
    public Result<?> likeComment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        interactionService.toggleCommentLike(id, userId);
        return Result.success("操作成功");
    }
}
