package com.cq.RssHub.service.impl;

import com.cq.RssHub.mapper.ArticleFavoriteMapper;
import com.cq.RssHub.mapper.ArticleMapper;
import com.cq.RssHub.pojo.Article;
import com.cq.RssHub.pojo.ArticleFavorite;
import com.cq.RssHub.pojo.vo.PageArticleFavoriteVO;
import com.cq.RssHub.service.ArticleFavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章收藏服务实现类
 */
@Service
public class ArticleFavoriteServiceImpl implements ArticleFavoriteService {
    private static final Logger logger = LoggerFactory.getLogger(ArticleFavoriteServiceImpl.class);
    
    @Autowired
    private ArticleFavoriteMapper articleFavoriteMapper;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Override
    @Transactional
    public boolean addFavorite(Integer userId, Integer articleId) {
        try {
            // 先检查是否已收藏
            if (isFavorited(userId, articleId)) {
                logger.info("用户{}已收藏文章{}", userId, articleId);
                return true; // 已收藏，直接返回成功
            }
            
            // 检查文章是否存在
            Article article = articleMapper.findById(articleId);
            if (article == null) {
                logger.warn("收藏失败：文章{}不存在", articleId);
                return false;
            }
            
            // 创建收藏记录
            ArticleFavorite favorite = new ArticleFavorite();
            favorite.setUserId(userId);
            favorite.setArticleId(articleId);
            favorite.setCreateTime(LocalDateTime.now());
            favorite.setUpdateTime(LocalDateTime.now());
            
            // 保存收藏
            int result = articleFavoriteMapper.insert(favorite);
            
            logger.info("用户{}收藏文章{}：{}", userId, articleId, result > 0 ? "成功" : "失败");
            return result > 0;
        } catch (Exception e) {
            logger.error("添加收藏失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean removeFavorite(Integer userId, Integer articleId) {
        try {
            int result = articleFavoriteMapper.delete(userId, articleId);
            logger.info("用户{}取消收藏文章{}：{}", userId, articleId, result > 0 ? "成功" : "失败");
            return result > 0;
        } catch (Exception e) {
            logger.error("取消收藏失败", e);
            return false;
        }
    }
    
    @Override
    public PageArticleFavoriteVO getUserFavorites(Integer userId, int page, int pageSize) {
        PageArticleFavoriteVO pageVO = new PageArticleFavoriteVO();
        try {
            // 计算总数
            int total = articleFavoriteMapper.countByUserId(userId);
            
            // 计算总页数
            int totalPages = (total + pageSize - 1) / pageSize;
            
            // 计算偏移量
            int offset = (page - 1) * pageSize;
            
            // 查询分页数据
            List<ArticleFavorite> favorites = articleFavoriteMapper.findByUserId(userId, offset, pageSize);
            
            // 设置分页数据
            pageVO.setFavorites(favorites);
            pageVO.setTotal(total);
            pageVO.setPage(page);
            pageVO.setPageSize(pageSize);
            pageVO.setTotalPages(totalPages);
            
            logger.info("获取用户{}收藏列表，页码：{}，每页：{}，总数：{}", userId, page, pageSize, total);
        } catch (Exception e) {
            logger.error("获取用户收藏列表失败", e);
        }
        return pageVO;
    }
    
    @Override
    public boolean isFavorited(Integer userId, Integer articleId) {
        try {
            ArticleFavorite favorite = articleFavoriteMapper.findByUserIdAndArticleId(userId, articleId);
            return favorite != null;
        } catch (Exception e) {
            logger.error("检查收藏状态失败", e);
            return false;
        }
    }
    
    @Override
    public List<ArticleFavorite> getRecentFavorites(Integer userId, int limit) {
        try {
            return articleFavoriteMapper.findRecentByUserId(userId, limit);
        } catch (Exception e) {
            logger.error("获取用户最近收藏失败", e);
            return List.of();
        }
    }
} 