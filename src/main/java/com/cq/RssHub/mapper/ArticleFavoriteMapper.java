package com.cq.RssHub.mapper;

import com.cq.RssHub.pojo.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章收藏数据访问接口
 */
@Mapper
public interface ArticleFavoriteMapper {
    /**
     * 添加收藏
     */
    int insert(ArticleFavorite favorite);
    
    /**
     * 删除收藏
     */
    int delete(@Param("userId") Integer userId, @Param("articleId") Integer articleId);
    
    /**
     * 查询用户所有收藏
     */
    List<ArticleFavorite> findByUserId(@Param("userId") Integer userId, @Param("offset") Integer offset, @Param("limit") Integer limit);
    
    /**
     * 计算用户收藏总数
     */
    int countByUserId(@Param("userId") Integer userId);
    
    /**
     * 检查用户是否已收藏该文章
     */
    ArticleFavorite findByUserIdAndArticleId(@Param("userId") Integer userId, @Param("articleId") Integer articleId);
    
    /**
     * 获取用户最近的收藏文章
     */
    List<ArticleFavorite> findRecentByUserId(@Param("userId") Integer userId, @Param("limit") Integer limit);
    
    /**
     * 获取文章的收藏数量
     */
    int countByArticleId(@Param("articleId") Integer articleId);
} 