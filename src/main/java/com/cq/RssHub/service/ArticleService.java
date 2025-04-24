package com.cq.RssHub.service;

import com.cq.RssHub.pojo.Article;
import com.cq.RssHub.pojo.vo.PageArticleVO;
import java.time.LocalDateTime;
import java.util.List;

public interface ArticleService {
    /**
     * 获取文章列表
     * @param keyword 搜索关键词
     * @param sourceId RSS源ID
     * @param categoryId 分类ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页文章列表
     */
    PageArticleVO getArticles(String keyword, Integer sourceId, Integer categoryId, 
                             LocalDateTime startDate, LocalDateTime endDate,
                             Integer page, Integer pageSize);
    
    /**
     * 获取文章总数
     * @param keyword 搜索关键词
     * @param sourceId RSS源ID
     * @param categoryId 分类ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 文章总数
     */
    int getArticlesCount(String keyword, Integer sourceId, Integer categoryId,
                        LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 根据ID获取文章详情
     * @param id 文章ID
     * @return 文章详情
     */
    Article getArticleById(Integer id);
    
    /**
     * 获取最新文章列表
     * @param limit 获取数量
     * @return 最新文章列表
     */
    List<Article> getLatestArticles(Integer limit);
    
    /**
     * 创建文章
     * @param article 文章信息
     * @return 创建的文章
     */
    Article createArticle(Article article);
    
    /**
     * 更新文章
     * @param article 文章信息
     * @return 是否更新成功
     */
    boolean updateArticle(Article article);
    
    /**
     * 增加阅读次数
     * @param id 文章ID
     * @return 是否增加成功
     */
    boolean incrementReadCount(Integer id);
    
    /**
     * 删除文章
     * @param id 文章ID
     * @return 是否删除成功
     */
    boolean deleteArticle(Integer id);
    
    /**
     * 批量删除文章
     * @param ids 文章ID列表
     * @return 是否删除成功
     */
    boolean deleteArticles(List<Integer> ids);
    
    /**
     * 删除过期文章
     * @param days 保留天数
     * @return 删除的文章数量
     */
    int deleteExpiredArticles(int days);
    
    /**
     * 删除过期文章
     * @param cutoffDate 截止日期
     * @return 删除的文章数量
     */
    int deleteExpiredArticles(LocalDateTime cutoffDate);
    
    /**
     * 保存文章列表
     * @param articles 文章列表
     * @return 保存的文章列表
     */
    List<Article> saveArticles(List<Article> articles);
} 