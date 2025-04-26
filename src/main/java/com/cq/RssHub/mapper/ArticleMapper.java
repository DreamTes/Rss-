package com.cq.RssHub.mapper;

import com.cq.RssHub.pojo.Article;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ArticleMapper {
    /**
     * 条件查询文章列表
     * 注意：当使用PageHelper时，offset和limit参数不再需要
     */
    List<Article> findByFilters(@Param("keyword") String keyword, 
                              @Param("sourceId") Integer sourceId, 
                              @Param("categoryId") Integer categoryId,
                              @Param("startDate") LocalDateTime startDate, 
                              @Param("endDate") LocalDateTime endDate,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);
    
    /**
     * 获取符合条件的文章总数
     */
    int countByFilters(@Param("keyword") String keyword, 
                     @Param("sourceId") Integer sourceId, 
                     @Param("categoryId") Integer categoryId,
                     @Param("startDate") LocalDateTime startDate, 
                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根据ID查询文章
     */
    Article findById(Integer id);
    
    /**
     * 获取最新文章列表
     */
    List<Article> findLatestArticles(Integer limit);
    
    /**
     * 插入文章
     */
    int insert(Article article);
    
    /**
     * 更新文章
     */
    int update(Article article);
    
    /**
     * 增加阅读计数
     */
    int incrementReadCount(Integer id);
    
    /**
     * 删除文章
     */
    int deleteById(Integer id);
    
    /**
     * 批量删除文章
     */
    int deleteByIds(@Param("ids") List<Integer> ids);
    
    /**
     * 删除过期文章
     */
    int deleteOlderThan(LocalDateTime cutoffDate);
    
    /**
     * 查询已存在的链接
     */
    List<String> findExistingLinks(List<String> links);
    
    /**
     * 批量插入文章
     */
    int batchInsert(@Param("articles") List<Article> articles);
    
    /**
     * 统计文章总数
     */
    @Select("SELECT COUNT(*) FROM article")
    int countTotal();
    
    /**
     * 根据日期范围统计文章数
     */
    @Select("SELECT COUNT(*) FROM article WHERE create_time BETWEEN #{startDate} AND #{endDate}")
    int countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查找所有没有封面图的文章
     * @return 没有封面图的文章列表
     */
    List<Article> findArticlesWithoutCoverImage();
} 