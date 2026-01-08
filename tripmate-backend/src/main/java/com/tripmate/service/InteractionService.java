package com.tripmate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tripmate.dto.CommentRequest;
import com.tripmate.entity.*;
import com.tripmate.exception.BusinessException;
import com.tripmate.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class InteractionService {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RouteMapper routeMapper;

    @Autowired
    private AttractionMapper attractionMapper;

    @Transactional
    public void toggleLike(String targetType, Long targetId, Long userId) {
        if (!"route".equals(targetType) && !"attraction".equals(targetType)) {
            throw new BusinessException("无效的目标类型");
        }

        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getUserId, userId)
                .eq(Like::getTargetType, targetType)
                .eq(Like::getTargetId, targetId);
        Like existLike = likeMapper.selectOne(queryWrapper);

        if (existLike != null) {
            likeMapper.deleteById(existLike.getId());
            decrementLikesCount(targetType, targetId);
        } else {
            Like like = new Like();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            likeMapper.insert(like);
            incrementLikesCount(targetType, targetId);
        }
    }

    @Transactional
    public void toggleFavorite(String targetType, Long targetId, Long userId) {
        if (!"route".equals(targetType) && !"attraction".equals(targetType)) {
            throw new BusinessException("无效的目标类型");
        }

        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId);
        Favorite existFavorite = favoriteMapper.selectOne(queryWrapper);

        if (existFavorite != null) {
            favoriteMapper.deleteById(existFavorite.getId());
            decrementFavoritesCount(targetType, targetId);
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setTargetType(targetType);
            favorite.setTargetId(targetId);
            favoriteMapper.insert(favorite);
            incrementFavoritesCount(targetType, targetId);
        }
    }

    @Transactional
    public Long addComment(String targetType, Long targetId, CommentRequest request, Long userId) {
        if (!"route".equals(targetType) && !"attraction".equals(targetType)) {
            throw new BusinessException("无效的目标类型");
        }

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);
        comment.setContent(request.getContent());
        commentMapper.insert(comment);

        incrementCommentsCount(targetType, targetId);

        return comment.getId();
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (comment.getUserId() == null) {
            throw new BusinessException("评论数据异常：用户ID为空");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论（评论作者ID: " + comment.getUserId() + ", 当前用户ID: " + userId + "）");
        }
        
        commentMapper.deleteById(commentId);
        decrementCommentsCount(comment.getTargetType(), comment.getTargetId());
    }
    
    @Transactional
    public void toggleCommentLike(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 注意：这里简化处理，直接增加点赞数
        // 如果需要防止重复点赞，可以添加comment_likes表来记录点赞关系
        int currentLikes = comment.getLikesCount() != null ? comment.getLikesCount() : 0;
        comment.setLikesCount(currentLikes + 1);
        commentMapper.updateById(comment);
    }

    private void decrementCommentsCount(String targetType, Long targetId) {
        if ("route".equals(targetType)) {
            Route route = routeMapper.selectById(targetId);
            if (route != null && route.getCommentsCount() > 0) {
                route.setCommentsCount(route.getCommentsCount() - 1);
                routeMapper.updateById(route);
            }
        } else if ("attraction".equals(targetType)) {
            Attraction attraction = attractionMapper.selectById(targetId);
            if (attraction != null && attraction.getCommentsCount() > 0) {
                attraction.setCommentsCount(attraction.getCommentsCount() - 1);
                attractionMapper.updateById(attraction);
            }
        }
    }

    public List<?> getComments(String targetType, Long targetId) {
        if (!"route".equals(targetType) && !"attraction".equals(targetType)) {
            throw new BusinessException("无效的目标类型");
        }
        
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("无效的目标ID");
        }

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .orderByDesc(Comment::getCreatedAt);

        List<Comment> comments = commentMapper.selectList(queryWrapper);
        return comments.stream().map(comment -> {
            User user = userMapper.selectById(comment.getUserId());
            Map<String, Object> commentData = new java.util.HashMap<>();
            commentData.put("id", comment.getId());
            commentData.put("content", comment.getContent());
            commentData.put("time", formatTime(comment.getCreatedAt()));
            commentData.put("likes", comment.getLikesCount() != null ? comment.getLikesCount() : 0);
            commentData.put("isLiked", false); // TODO: 根据当前用户查询点赞状态
            commentData.put("userId", comment.getUserId()); // 添加userId，用于前端判断是否可以删除
            
            // 使用 HashMap 而不是 Map.of，避免 null 值问题
            Map<String, Object> userMap = new java.util.HashMap<>();
            if (user != null) {
                userMap.put("name", user.getUsername());
                userMap.put("username", user.getUsername()); // 同时提供两个字段名
                userMap.put("avatar", user.getAvatar() != null ? user.getAvatar() : "https://i.pravatar.cc/150?img=1");
            } else {
                userMap.put("name", "匿名用户");
                userMap.put("username", "匿名用户");
                userMap.put("avatar", "https://i.pravatar.cc/150?img=1");
            }
            commentData.put("user", userMap);
            
            return commentData;
        }).collect(java.util.stream.Collectors.toList());
    }

    private String formatTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "刚刚";
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long days = java.time.temporal.ChronoUnit.DAYS.between(dateTime, now);
        if (days == 0) {
            long hours = java.time.temporal.ChronoUnit.HOURS.between(dateTime, now);
            if (hours == 0) {
                long minutes = java.time.temporal.ChronoUnit.MINUTES.between(dateTime, now);
                if (minutes == 0) {
                    return "刚刚";
                }
                return minutes + "分钟前";
            }
            return hours + "小时前";
        } else if (days == 1) {
            return "昨天";
        } else if (days < 7) {
            return days + "天前";
        } else {
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    private void incrementLikesCount(String targetType, Long targetId) {
        if ("route".equals(targetType)) {
            Route route = routeMapper.selectById(targetId);
            if (route != null) {
                route.setLikesCount(route.getLikesCount() + 1);
                routeMapper.updateById(route);
            }
        } else if ("attraction".equals(targetType)) {
            Attraction attraction = attractionMapper.selectById(targetId);
            if (attraction != null) {
                attraction.setLikesCount(attraction.getLikesCount() + 1);
                attractionMapper.updateById(attraction);
            }
        }
    }

    private void decrementLikesCount(String targetType, Long targetId) {
        if ("route".equals(targetType)) {
            Route route = routeMapper.selectById(targetId);
            if (route != null && route.getLikesCount() > 0) {
                route.setLikesCount(route.getLikesCount() - 1);
                routeMapper.updateById(route);
            }
        } else if ("attraction".equals(targetType)) {
            Attraction attraction = attractionMapper.selectById(targetId);
            if (attraction != null && attraction.getLikesCount() > 0) {
                attraction.setLikesCount(attraction.getLikesCount() - 1);
                attractionMapper.updateById(attraction);
            }
        }
    }

    private void incrementFavoritesCount(String targetType, Long targetId) {
        if ("route".equals(targetType)) {
            Route route = routeMapper.selectById(targetId);
            if (route != null) {
                route.setFavoritesCount(route.getFavoritesCount() + 1);
                routeMapper.updateById(route);
            }
        } else if ("attraction".equals(targetType)) {
            Attraction attraction = attractionMapper.selectById(targetId);
            if (attraction != null) {
                attraction.setFavoritesCount(attraction.getFavoritesCount() + 1);
                attractionMapper.updateById(attraction);
            }
        }
    }

    private void decrementFavoritesCount(String targetType, Long targetId) {
        if ("route".equals(targetType)) {
            Route route = routeMapper.selectById(targetId);
            if (route != null && route.getFavoritesCount() > 0) {
                route.setFavoritesCount(route.getFavoritesCount() - 1);
                routeMapper.updateById(route);
            }
        } else if ("attraction".equals(targetType)) {
            Attraction attraction = attractionMapper.selectById(targetId);
            if (attraction != null && attraction.getFavoritesCount() > 0) {
                attraction.setFavoritesCount(attraction.getFavoritesCount() - 1);
                attractionMapper.updateById(attraction);
            }
        }
    }

    private void incrementCommentsCount(String targetType, Long targetId) {
        if ("route".equals(targetType)) {
            Route route = routeMapper.selectById(targetId);
            if (route != null) {
                route.setCommentsCount(route.getCommentsCount() + 1);
                routeMapper.updateById(route);
            }
        } else if ("attraction".equals(targetType)) {
            Attraction attraction = attractionMapper.selectById(targetId);
            if (attraction != null) {
                attraction.setCommentsCount(attraction.getCommentsCount() + 1);
                attractionMapper.updateById(attraction);
            }
        }
    }
}
