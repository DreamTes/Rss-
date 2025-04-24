package com.cq.RssHub.service.impl;

import com.cq.RssHub.mapper.ArticleMapper;
import com.cq.RssHub.pojo.Article;
import com.cq.RssHub.pojo.vo.PageArticleVO;
import com.cq.RssHub.service.ArticleService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public PageArticleVO getArticles(String keyword, Integer sourceId, Integer categoryId, 
                                    LocalDateTime startDate, LocalDateTime endDate, 
                                    Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        // 使用PageHelper进行分页
        PageHelper.startPage(page, pageSize);
        List<Article> articles = articleMapper.findByFilters(keyword, sourceId, categoryId, startDate, endDate, null, null);
        
        // 获取分页信息
        long total = ((Page<Article>) articles).getTotal();
        
        // 构建并返回PageArticleVO
        return new PageArticleVO(total, articles);
    }

    @Override
    public int getArticlesCount(String keyword, Integer sourceId, Integer categoryId, 
                               LocalDateTime startDate, LocalDateTime endDate) {
        return articleMapper.countByFilters(keyword, sourceId, categoryId, startDate, endDate);
    }

    @Override
    public Article getArticleById(Integer id) {
        return articleMapper.findById(id);
    }

    @Override
    public List<Article> getLatestArticles(Integer limit) {
        if (limit == null || limit < 1) {
            limit = 10;
        }
        return articleMapper.findLatestArticles(limit);
    }

    @Override
    @Transactional
    public Article createArticle(Article article) {
        if (article == null) {
            return null;
        }
        
        // 设置初始值
        if (article.getIsRead() == null) {
            article.setIsRead(false);
        }
        if (article.getIsStarred() == null) {
            article.setIsStarred(false);
        }
        if (article.getReadCount() == null) {
            article.setReadCount(0);
        }
        
        LocalDateTime now = LocalDateTime.now();
        article.setCreateTime(now);
        article.setUpdateTime(now);
        
        articleMapper.insert(article);
        return article;
    }

    @Override
    @Transactional
    public boolean updateArticle(Article article) {
        if (article == null || article.getId() == null) {
            return false;
        }
        
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.update(article);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteArticle(Integer id) {
        if (id == null) {
            return false;
        }
        
        articleMapper.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteArticles(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        articleMapper.deleteByIds(ids);
        return true;
    }

    @Override
    public int deleteExpiredArticles(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return deleteExpiredArticles(cutoffDate);
    }

    @Override
    @Transactional
    public boolean incrementReadCount(Integer id) {
        if (id == null) {
            return false;
        }
        
        articleMapper.incrementReadCount(id);
        return true;
    }

    @Override
    @Transactional
    public int deleteExpiredArticles(LocalDateTime cutoffDate) {
        if (cutoffDate == null) {
            return 0;
        }
        
        return articleMapper.deleteOlderThan(cutoffDate);
    }

    @Override
    @Transactional
    public List<Article> saveArticles(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 提取所有链接
        List<String> links = new ArrayList<>();
        for (Article article : articles) {
            links.add(article.getLink());
        }
        
        // 查找已存在的链接
        List<String> existingLinks = articleMapper.findExistingLinks(links);
        
        // 过滤掉已存在的文章
        List<Article> newArticles = new ArrayList<>();
        for (Article article : articles) {
            if (!existingLinks.contains(article.getLink())) {
                // 设置初始值
                if (article.getIsRead() == null) {
                    article.setIsRead(false);
                }
                if (article.getIsStarred() == null) {
                    article.setIsStarred(false);
                }
                if (article.getReadCount() == null) {
                    article.setReadCount(0);
                }
                
                LocalDateTime now = LocalDateTime.now();
                article.setCreateTime(now);
                article.setUpdateTime(now);
                
                newArticles.add(article);
            }
        }
        
        // 批量插入新文章
        if (!newArticles.isEmpty()) {
            articleMapper.batchInsert(newArticles);
        }
        
        return newArticles;
    }
}
