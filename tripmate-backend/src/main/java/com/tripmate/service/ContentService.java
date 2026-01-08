package com.tripmate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tripmate.dto.*;
import com.tripmate.entity.*;
import com.tripmate.exception.BusinessException;
import com.tripmate.mapper.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentService {

    @Autowired
    private RouteMapper routeMapper;

    @Autowired
    private AttractionMapper attractionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CommentMapper commentMapper;

    public IPage<ContentItem> getContentList(String type, String sortBy, Integer page, Integer size) {
        Page<Route> routePage = new Page<>(page, size);
        Page<Attraction> attractionPage = new Page<>(page, size);

        if ("route".equals(type)) {
            LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Route::getIsDraft, false).or().isNull(Route::getIsDraft); // 只显示已发布的
            setOrderBy(wrapper, sortBy, Route::getLikesCount, Route::getFavoritesCount, Route::getCreatedAt);
            IPage<Route> result = routeMapper.selectPage(routePage, wrapper);
            return convertRoutesToContentPage(result);
        } else if ("attraction".equals(type)) {
            LambdaQueryWrapper<Attraction> wrapper = new LambdaQueryWrapper<>();
            setOrderBy(wrapper, sortBy, Attraction::getLikesCount, Attraction::getFavoritesCount, Attraction::getCreatedAt);
            IPage<Attraction> result = attractionMapper.selectPage(attractionPage, wrapper);
            return convertAttractionsToContentPage(result);
        } else {
            Page<ContentItem> contentPage = new Page<>(page, size);
            
            LambdaQueryWrapper<Route> routeWrapper = new LambdaQueryWrapper<>();
            routeWrapper.eq(Route::getIsDraft, false).or().isNull(Route::getIsDraft); // 只显示已发布的
            setOrderBy(routeWrapper, sortBy, Route::getLikesCount, Route::getFavoritesCount, Route::getCreatedAt);
            IPage<Route> routes = routeMapper.selectPage(routePage, routeWrapper);

            LambdaQueryWrapper<Attraction> attractionWrapper = new LambdaQueryWrapper<>();
            setOrderBy(attractionWrapper, sortBy, Attraction::getLikesCount, Attraction::getFavoritesCount, Attraction::getCreatedAt);
            IPage<Attraction> attractions = attractionMapper.selectPage(attractionPage, attractionWrapper);

            List<ContentItem> items = new java.util.ArrayList<>();
            items.addAll(convertRoutesToContentItems(routes.getRecords()));
            items.addAll(convertAttractionsToContentItems(attractions.getRecords()));

            items.sort((a, b) -> {
                if ("hot".equals(sortBy)) {
                    return b.getLikes().compareTo(a.getLikes());
                } else if ("favorite".equals(sortBy)) {
                    return b.getFavorites().compareTo(a.getFavorites());
                } else {
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                }
            });

            contentPage.setRecords(items);
            contentPage.setTotal(routes.getTotal() + attractions.getTotal());
            contentPage.setPages((long) Math.ceil((double) contentPage.getTotal() / size));
            contentPage.setCurrent(page);
            contentPage.setSize(size);

            return contentPage;
        }
    }

    private <T> void setOrderBy(LambdaQueryWrapper<T> wrapper, String sortBy, 
                                  SFunction<T, ?> likesField,
                                  SFunction<T, ?> favoritesField,
                                  SFunction<T, ?> createdAtField) {
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(likesField);
        } else if ("favorite".equals(sortBy)) {
            wrapper.orderByDesc(favoritesField);
        } else {
            wrapper.orderByDesc(createdAtField);
        }
    }

    private IPage<ContentItem> convertRoutesToContentPage(IPage<Route> routes) {
        IPage<ContentItem> page = new Page<>(routes.getCurrent(), routes.getSize(), routes.getTotal());
        page.setRecords(convertRoutesToContentItems(routes.getRecords()));
        return page;
    }

    private IPage<ContentItem> convertAttractionsToContentPage(IPage<Attraction> attractions) {
        IPage<ContentItem> page = new Page<>(attractions.getCurrent(), attractions.getSize(), attractions.getTotal());
        page.setRecords(convertAttractionsToContentItems(attractions.getRecords()));
        return page;
    }

    private List<ContentItem> convertRoutesToContentItems(List<Route> routes) {
        return routes.stream().map(route -> {
            ContentItem item = new ContentItem();
            BeanUtils.copyProperties(route, item);
            item.setType("route");
            item.setUserId(route.getUserId()); // 设置作者ID
            
            User user = userMapper.selectById(route.getUserId());
            if (user != null) {
                item.setAuthor(user.getUsername());
                item.setAvatar(user.getAvatar());
            }

            if (StringUtils.hasText(route.getTags())) {
                item.setTags(Arrays.asList(route.getTags().split(",")));
            }

            item.setLikes(route.getLikesCount());
            item.setFavorites(route.getFavoritesCount());
            item.setComments(route.getCommentsCount());

            return item;
        }).collect(Collectors.toList());
    }

    private List<ContentItem> convertAttractionsToContentItems(List<Attraction> attractions) {
        return attractions.stream().map(attraction -> {
            ContentItem item = new ContentItem();
            BeanUtils.copyProperties(attraction, item);
            item.setType("attraction");
            item.setUserId(attraction.getUserId()); // 设置作者ID

            User user = userMapper.selectById(attraction.getUserId());
            if (user != null) {
                item.setAuthor(user.getUsername());
                item.setAvatar(user.getAvatar());
            }

            if (StringUtils.hasText(attraction.getTags())) {
                item.setTags(Arrays.asList(attraction.getTags().split(",")));
            }

            item.setLikes(attraction.getLikesCount());
            item.setFavorites(attraction.getFavoritesCount());
            item.setComments(attraction.getCommentsCount());
            
            // 设置景点特有的字段
            item.setOpenTime(attraction.getOpenTime());
            item.setTicketPrice(attraction.getTicketPrice());
            item.setSuggestedDuration(attraction.getSuggestedDuration());

            return item;
        }).collect(Collectors.toList());
    }

    public ContentItem getRouteDetail(Long id, Long currentUserId) {
        Route route = routeMapper.selectById(id);
        if (route == null) {
            throw new BusinessException("线路不存在");
        }

        ContentItem item = new ContentItem();
        BeanUtils.copyProperties(route, item);
        item.setType("route");
        item.setUserId(route.getUserId()); // 设置作者ID

        User user = userMapper.selectById(route.getUserId());
        if (user != null) {
            item.setAuthor(user.getUsername());
            item.setAvatar(user.getAvatar());
        }

        if (StringUtils.hasText(route.getTags())) {
            item.setTags(Arrays.asList(route.getTags().split(",")));
        }

        item.setLikes(route.getLikesCount());
        item.setFavorites(route.getFavoritesCount());
        item.setComments(route.getCommentsCount());

        return item;
    }

    public ContentItem getAttractionDetail(Long id, Long currentUserId) {
        Attraction attraction = attractionMapper.selectById(id);
        if (attraction == null) {
            throw new BusinessException("景点不存在");
        }

        ContentItem item = new ContentItem();
        BeanUtils.copyProperties(attraction, item);
        item.setType("attraction");
        item.setUserId(attraction.getUserId()); // 设置作者ID

        User user = userMapper.selectById(attraction.getUserId());
        if (user != null) {
            item.setAuthor(user.getUsername());
            item.setAvatar(user.getAvatar());
        }

        if (StringUtils.hasText(attraction.getTags())) {
            item.setTags(Arrays.asList(attraction.getTags().split(",")));
        }

        item.setLikes(attraction.getLikesCount());
        item.setFavorites(attraction.getFavoritesCount());
        item.setComments(attraction.getCommentsCount());
        
        // 设置景点特有的字段
        item.setOpenTime(attraction.getOpenTime());
        item.setTicketPrice(attraction.getTicketPrice());
        item.setSuggestedDuration(attraction.getSuggestedDuration());

        return item;
    }

    @Transactional
    public Long createRoute(CreateRouteRequest request, Long userId) {
        Route route = new Route();
        route.setUserId(userId);
        route.setTitle(request.getTitle());
        route.setCover(request.getCover());
        route.setDescription(request.getDescription());
        route.setDuration(request.getDuration());
        route.setBudget(request.getBudget());
        route.setTips(request.getTips());
        route.setIsDraft(request.getIsDraft() != null && request.getIsDraft());
        
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            route.setTags(String.join(",", request.getTags()));
        }

        routeMapper.insert(route);
        return route.getId();
    }
    
    @Transactional
    public void updateRoute(Long routeId, CreateRouteRequest request, Long userId) {
        Route route = routeMapper.selectById(routeId);
        if (route == null) {
            throw new BusinessException("路线不存在");
        }
        if (!route.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此路线");
        }
        
        route.setTitle(request.getTitle());
        route.setCover(request.getCover());
        route.setDescription(request.getDescription());
        route.setDuration(request.getDuration());
        route.setBudget(request.getBudget());
        route.setTips(request.getTips());
        if (request.getIsDraft() != null) {
            route.setIsDraft(request.getIsDraft());
        }
        
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            route.setTags(String.join(",", request.getTags()));
        }
        
        routeMapper.updateById(route);
    }
    
    public IPage<ContentItem> getDraftRoutes(Long userId, Integer page, Integer size) {
        IPage<Route> routePage = new Page<>(page, size);
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Route::getUserId, userId);
        wrapper.eq(Route::getIsDraft, true);
        wrapper.orderByDesc(Route::getUpdatedAt);
        IPage<Route> routes = routeMapper.selectPage(routePage, wrapper);
        return convertRoutesToContentPage(routes);
    }

    @Transactional
    public Long createAttraction(CreateAttractionRequest request, Long userId) {
        Attraction attraction = new Attraction();
        attraction.setUserId(userId);
        attraction.setTitle(request.getTitle());
        attraction.setCover(request.getCover());
        attraction.setDescription(request.getDescription());
        attraction.setLocation(request.getLocation());
        attraction.setRating(request.getRating());
        
        if (request.getOpenTime() != null) {
            attraction.setOpenTime(request.getOpenTime());
        }
        if (request.getTicketPrice() != null) {
            attraction.setTicketPrice(request.getTicketPrice());
        }
        if (request.getSuggestedDuration() != null) {
            attraction.setSuggestedDuration(request.getSuggestedDuration());
        }
        
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            attraction.setTags(String.join(",", request.getTags()));
        }

        attractionMapper.insert(attraction);
        return attraction.getId();
    }
    
    @Transactional
    public void updateAttraction(Long attractionId, CreateAttractionRequest request, Long userId) {
        Attraction attraction = attractionMapper.selectById(attractionId);
        if (attraction == null) {
            throw new BusinessException("景点不存在");
        }
        if (!attraction.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此景点");
        }
        
        attraction.setTitle(request.getTitle());
        attraction.setCover(request.getCover());
        attraction.setDescription(request.getDescription());
        attraction.setLocation(request.getLocation());
        attraction.setRating(request.getRating());
        
        if (request.getOpenTime() != null) {
            attraction.setOpenTime(request.getOpenTime());
        }
        if (request.getTicketPrice() != null) {
            attraction.setTicketPrice(request.getTicketPrice());
        }
        if (request.getSuggestedDuration() != null) {
            attraction.setSuggestedDuration(request.getSuggestedDuration());
        }
        
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            attraction.setTags(String.join(",", request.getTags()));
        }
        
        attractionMapper.updateById(attraction);
    }
}
