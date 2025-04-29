package com.cq.RssHub.controller;

import com.cq.RssHub.pojo.Article;
import com.cq.RssHub.pojo.ArticleFavorite;
import com.cq.RssHub.pojo.DTO.ArticleFavoriteDTO;
import com.cq.RssHub.pojo.ResponseMessage;
import com.cq.RssHub.pojo.vo.PageArticleFavoriteVO;
import com.cq.RssHub.service.ArticleFavoriteService;
import com.cq.RssHub.service.ArticleRecommendService;
import com.cq.RssHub.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章收藏控制器
 */
@RestController
@RequestMapping("/rss/favorites")
public class ArticleFavoriteController {
    
    @Autowired
    private ArticleFavoriteService articleFavoriteService;
    
    @Autowired
    private ArticleRecommendService articleRecommendService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取用户收藏列表
     */
    @GetMapping
    public ResponseMessage<?> getUserFavorites(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return ResponseMessage.unauthorized("未登录");
            }
            
            // 从token中获取用户ID（这里假设getUserIdFromToken方法能从token中解析用户ID）
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseMessage.error("无法获取用户ID");
            }
            
            PageArticleFavoriteVO pageVO = articleFavoriteService.getUserFavorites(userId, page, pageSize);
            return ResponseMessage.success("获取收藏列表成功", pageVO);
        } catch (Exception e) {
            return ResponseMessage.error("获取收藏列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加收藏
     */
    @PostMapping
    public ResponseMessage<?> addFavorite(
            @RequestHeader("Authorization") String token,
            @RequestBody ArticleFavoriteDTO favoriteDTO) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return ResponseMessage.unauthorized("未登录");
            }
            
            // 从token中获取用户ID
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseMessage.error("无法获取用户ID");
            }
            
            // 设置用户ID
            favoriteDTO.setUserId(userId);
            
            boolean result = articleFavoriteService.addFavorite(userId, favoriteDTO.getArticleId());
            if (result) {
                // 检查收藏状态
                boolean isFavorited = articleFavoriteService.isFavorited(userId, favoriteDTO.getArticleId());
                
                Map<String, Object> data = new HashMap<>();
                data.put("favorited", isFavorited);
                
                return ResponseMessage.success("收藏成功", data);
            } else {
                return ResponseMessage.error("收藏失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消收藏
     */
    @DeleteMapping("/{articleId}")
    public ResponseMessage<?> removeFavorite(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer articleId) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return ResponseMessage.unauthorized("未登录");
            }
            
            // 从token中获取用户ID
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseMessage.error("无法获取用户ID");
            }
            
            boolean result = articleFavoriteService.removeFavorite(userId, articleId);
            if (result) {
                // 检查收藏状态
                boolean isFavorited = articleFavoriteService.isFavorited(userId, articleId);
                
                Map<String, Object> data = new HashMap<>();
                data.put("favorited", isFavorited);
                
                return ResponseMessage.success("取消收藏成功", data);
            } else {
                return ResponseMessage.error("取消收藏失败");
            }
        } catch (Exception e) {
            return ResponseMessage.error("取消收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查文章是否已收藏
     */
    @GetMapping("/check/{articleId}")
    public ResponseMessage<?> checkFavorite(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer articleId) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return ResponseMessage.unauthorized("未登录");
            }
            
            // 从token中获取用户ID
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseMessage.error("无法获取用户ID");
            }
            
            boolean isFavorited = articleFavoriteService.isFavorited(userId, articleId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("favorited", isFavorited);
            
            return ResponseMessage.success("检查收藏状态成功", data);
        } catch (Exception e) {
            return ResponseMessage.error("检查收藏状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户个性化推荐
     */
    @GetMapping("/recommendations")
    public ResponseMessage<?> getRecommendations(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return ResponseMessage.unauthorized("未登录");
            }
            
            // 从token中获取用户ID
            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseMessage.error("无法获取用户ID");
            }
            
            List<Article> recommendations = articleRecommendService.getPersonalizedRecommendations(userId, limit);
            return ResponseMessage.success("获取推荐成功", recommendations);
        } catch (Exception e) {
            return ResponseMessage.error("获取推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取相似文章推荐
     */
    @GetMapping("/similar/{articleId}")
    public ResponseMessage<?> getSimilarArticles(
            @PathVariable Integer articleId,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        try {
            List<Article> similarArticles = articleRecommendService.getSimilarArticles(articleId, limit);
            return ResponseMessage.success("获取相似文章成功", similarArticles);
        } catch (Exception e) {
            return ResponseMessage.error("获取相似文章失败: " + e.getMessage());
        }
    }
} 