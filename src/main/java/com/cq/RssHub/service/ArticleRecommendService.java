package com.cq.RssHub.service;

import com.cq.RssHub.pojo.Article;
import java.util.List;

/**
 * 文章推荐服务接口
 */
public interface ArticleRecommendService {
    
    /**
     * 基于用户收藏的个性化文章推荐
     * @param userId 用户ID
     * @param limit 推荐数量限制
     * @return 推荐文章列表
     */
    List<Article> getPersonalizedRecommendations(Integer userId, int limit);
    
    /**
     * 获取与指定文章相似的文章推荐
     * @param articleId 文章ID
     * @param limit 推荐数量限制
     * @return 相似文章列表
     */
    List<Article> getSimilarArticles(Integer articleId, int limit);
    
    /**
     * 获取热门文章推荐
     * @param limit 推荐数量限制
     * @return 热门文章列表
     */
    List<Article> getHotRecommendations(int limit);
} 