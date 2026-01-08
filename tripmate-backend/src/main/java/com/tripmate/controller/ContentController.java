package com.tripmate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tripmate.annotation.RequireAuth;
import com.tripmate.common.Result;
import com.tripmate.dto.*;
import com.tripmate.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/list")
    public Result<IPage<ContentItem>> getContentList(
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        IPage<ContentItem> result = contentService.getContentList(type, sortBy, page, size);
        return Result.success(result);
    }

    @GetMapping("/route/{id}")
    public Result<ContentItem> getRouteDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ContentItem result = contentService.getRouteDetail(id, userId);
        return Result.success(result);
    }

    @GetMapping("/attraction/{id}")
    public Result<ContentItem> getAttractionDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ContentItem result = contentService.getAttractionDetail(id, userId);
        return Result.success(result);
    }

    @PostMapping("/route")
    @RequireAuth
    public Result<Long> createRoute(
            @RequestBody CreateRouteRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long routeId = contentService.createRoute(request, userId);
        return Result.success(routeId);
    }

    @PostMapping("/attraction")
    @RequireAuth
    public Result<Long> createAttraction(
            @RequestBody CreateAttractionRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long attractionId = contentService.createAttraction(request, userId);
        return Result.success(attractionId);
    }
    
    @PutMapping("/route/{id}")
    @RequireAuth
    public Result<?> updateRoute(
            @PathVariable Long id,
            @RequestBody CreateRouteRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        contentService.updateRoute(id, request, userId);
        return Result.success("更新成功");
    }
    
    @PutMapping("/attraction/{id}")
    @RequireAuth
    public Result<?> updateAttraction(
            @PathVariable Long id,
            @RequestBody CreateAttractionRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        contentService.updateAttraction(id, request, userId);
        return Result.success("更新成功");
    }
    
    @GetMapping("/drafts")
    @RequireAuth
    public Result<IPage<ContentItem>> getDrafts(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        IPage<ContentItem> result = contentService.getDraftRoutes(userId, page, size);
        return Result.success(result);
    }
}
