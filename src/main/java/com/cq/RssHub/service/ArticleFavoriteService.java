package com.cq.RssHub.service;

import com.cq.RssHub.pojo.ArticleFavorite;
import com.cq.RssHub.pojo.vo.PageArticleFavoriteVO;

import java.util.List;

/**
 * 文章收藏服务接口
 */
public interface ArticleFavoriteService {
    
    /**
     * 添加收藏
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 是否成功
     */
    boolean addFavorite(Integer userId, Integer articleId);
    
    /**
     * 取消收藏
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 是否成功
     */
    boolean removeFavorite(Integer userId, Integer articleId);
    
    /**
     * 获取用户收藏列表（分页）
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 收藏分页对象
     */
    PageArticleFavoriteVO getUserFavorites(Integer userId, int page, int pageSize);
    
    /**
     * 检查文章是否已收藏
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 是否已收藏
     */
    boolean isFavorited(Integer userId, Integer articleId);
    
    /**
     * 获取用户最近收藏
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 收藏列表
     */
    List<ArticleFavorite> getRecentFavorites(Integer userId, int limit);
} 