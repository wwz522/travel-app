package com.tripmate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tripmate.dto.ContentItem;
import com.tripmate.dto.UpdateProfileRequest;
import com.tripmate.entity.Attraction;
import com.tripmate.entity.Favorite;
import com.tripmate.entity.Route;
import com.tripmate.entity.User;
import com.tripmate.exception.BusinessException;
import com.tripmate.mapper.AttractionMapper;
import com.tripmate.mapper.FavoriteMapper;
import com.tripmate.mapper.RouteMapper;
import com.tripmate.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserCenterService {

    @Autowired
    private RouteMapper routeMapper;

    @Autowired
    private AttractionMapper attractionMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private UserMapper userMapper;

    public User getUserProfile(Long userId) {
        return userMapper.selectById(userId);
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户名是否已被其他用户使用
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, request.getUsername())
                    .ne(User::getId, userId);
            User existUser = userMapper.selectOne(wrapper);
            if (existUser != null) {
                throw new BusinessException("用户名已被使用");
            }
            user.setUsername(request.getUsername());
        }

        // 检查邮箱是否已被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, request.getEmail())
                    .ne(User::getId, userId);
            User existUser = userMapper.selectOne(wrapper);
            if (existUser != null) {
                throw new BusinessException("邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        // 注意：这里假设User实体有bio字段，如果没有需要先添加到数据库和实体
        // user.setBio(request.getBio());

        userMapper.updateById(user);
    }

    public IPage<ContentItem> getUserPosts(Long userId, String type, Integer page, Integer size) {
        Page<ContentItem> pageParam = new Page<>(page, size);
        List<ContentItem> items = new ArrayList<>();
        long total = 0;

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if ("all".equals(type) || "route".equals(type)) {
            LambdaQueryWrapper<Route> routeWrapper = new LambdaQueryWrapper<>();
            routeWrapper.eq(Route::getUserId, userId)
                    .eq(Route::getIsDraft, false).or().isNull(Route::getIsDraft) // 只显示已发布的
                    .orderByDesc(Route::getCreatedAt);
            List<Route> routes = routeMapper.selectList(routeWrapper);
            
            for (Route route : routes) {
                ContentItem item = new ContentItem();
                item.setId(route.getId());
                item.setType("route");
                item.setTitle(route.getTitle());
                item.setCover(route.getCover());
                item.setAuthor(user.getUsername());
                item.setAvatar(user.getAvatar());
                item.setLikes(route.getLikesCount());
                item.setFavorites(route.getFavoritesCount());
                item.setComments(route.getCommentsCount());
                item.setTags(route.getTags() != null ? Arrays.asList(route.getTags().split(",")) : null);
                item.setDescription(route.getDescription());
                item.setDuration(route.getDuration());
                item.setBudget(route.getBudget());
                item.setCreatedAt(route.getCreatedAt());
                items.add(item);
            }
            total += routes.size();
        }

        if ("all".equals(type) || "attraction".equals(type)) {
            LambdaQueryWrapper<Attraction> attractionWrapper = new LambdaQueryWrapper<>();
            attractionWrapper.eq(Attraction::getUserId, userId)
                    .orderByDesc(Attraction::getCreatedAt);
            List<Attraction> attractions = attractionMapper.selectList(attractionWrapper);
            
            for (Attraction attraction : attractions) {
                ContentItem item = new ContentItem();
                item.setId(attraction.getId());
                item.setType("attraction");
                item.setTitle(attraction.getTitle());
                item.setCover(attraction.getCover());
                item.setAuthor(user.getUsername());
                item.setAvatar(user.getAvatar());
                item.setLikes(attraction.getLikesCount());
                item.setFavorites(attraction.getFavoritesCount());
                item.setComments(attraction.getCommentsCount());
                item.setTags(attraction.getTags() != null ? Arrays.asList(attraction.getTags().split(",")) : null);
                item.setDescription(attraction.getDescription());
                item.setRating(attraction.getRating());
                item.setLocation(attraction.getLocation());
                item.setCreatedAt(attraction.getCreatedAt());
                items.add(item);
            }
            total += attractions.size();
        }

        items.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        int start = (page - 1) * size;
        int end = Math.min(start + size, items.size());
        List<ContentItem> pageItems = items.subList(start, end);

        pageParam.setRecords(pageItems);
        pageParam.setTotal(total);
        return pageParam;
    }

    public IPage<ContentItem> getUserFavorites(Long userId, String type, Integer page, Integer size) {
        Page<ContentItem> pageParam = new Page<>(page, size);
        List<ContentItem> items = new ArrayList<>();
        long total = 0;

        LambdaQueryWrapper<Favorite> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(Favorite::getUserId, userId);
        
        if (!"all".equals(type)) {
            favoriteWrapper.eq(Favorite::getTargetType, type);
        }
        
        List<Favorite> favorites = favoriteMapper.selectList(favoriteWrapper);
        total = favorites.size();

        for (Favorite favorite : favorites) {
            ContentItem item = new ContentItem();
            item.setType(favorite.getTargetType());

            if ("route".equals(favorite.getTargetType())) {
                Route route = routeMapper.selectById(favorite.getTargetId());
                if (route != null) {
                    User user = userMapper.selectById(route.getUserId());
                    item.setId(route.getId());
                    item.setTitle(route.getTitle());
                    item.setCover(route.getCover());
                    item.setAuthor(user != null ? user.getUsername() : "未知用户");
                    item.setAvatar(user != null ? user.getAvatar() : null);
                    item.setLikes(route.getLikesCount());
                    item.setFavorites(route.getFavoritesCount());
                    item.setComments(route.getCommentsCount());
                    item.setTags(route.getTags() != null ? Arrays.asList(route.getTags().split(",")) : null);
                    item.setDescription(route.getDescription());
                    item.setDuration(route.getDuration());
                    item.setBudget(route.getBudget());
                    item.setCreatedAt(route.getCreatedAt());
                    items.add(item);
                }
            } else if ("attraction".equals(favorite.getTargetType())) {
                Attraction attraction = attractionMapper.selectById(favorite.getTargetId());
                if (attraction != null) {
                    User user = userMapper.selectById(attraction.getUserId());
                    item.setId(attraction.getId());
                    item.setTitle(attraction.getTitle());
                    item.setCover(attraction.getCover());
                    item.setAuthor(user != null ? user.getUsername() : "未知用户");
                    item.setAvatar(user != null ? user.getAvatar() : null);
                    item.setLikes(attraction.getLikesCount());
                    item.setFavorites(attraction.getFavoritesCount());
                    item.setComments(attraction.getCommentsCount());
                    item.setTags(attraction.getTags() != null ? Arrays.asList(attraction.getTags().split(",")) : null);
                    item.setDescription(attraction.getDescription());
                    item.setRating(attraction.getRating());
                    item.setLocation(attraction.getLocation());
                    item.setCreatedAt(attraction.getCreatedAt());
                    items.add(item);
                }
            }
        }

        items.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        int start = (page - 1) * size;
        int end = Math.min(start + size, items.size());
        List<ContentItem> pageItems = items.subList(start, end);

        pageParam.setRecords(pageItems);
        pageParam.setTotal(total);
        return pageParam;
    }

    @Transactional
    public void deletePost(Long id, Long userId) {
        Route route = routeMapper.selectById(id);
        if (route != null) {
            if (!route.getUserId().equals(userId)) {
                throw new BusinessException("无权删除该内容");
            }
            routeMapper.deleteById(id);
            return;
        }

        Attraction attraction = attractionMapper.selectById(id);
        if (attraction != null) {
            if (!attraction.getUserId().equals(userId)) {
                throw new BusinessException("无权删除该内容");
            }
            attractionMapper.deleteById(id);
            return;
        }

        throw new BusinessException("内容不存在");
    }

    @Transactional
    public void removeFavorite(Long id, Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetId, id);
        Favorite favorite = favoriteMapper.selectOne(wrapper);
        
        if (favorite == null) {
            throw new BusinessException("收藏不存在");
        }

        favoriteMapper.deleteById(favorite.getId());

        if ("route".equals(favorite.getTargetType())) {
            Route route = routeMapper.selectById(id);
            if (route != null) {
                route.setFavoritesCount(Math.max(0, route.getFavoritesCount() - 1));
                routeMapper.updateById(route);
            }
        } else if ("attraction".equals(favorite.getTargetType())) {
            Attraction attraction = attractionMapper.selectById(id);
            if (attraction != null) {
                attraction.setFavoritesCount(Math.max(0, attraction.getFavoritesCount() - 1));
                attractionMapper.updateById(attraction);
            }
        }
    }
}
