package com.cq.RssHub.service.impl;

import com.cq.RssHub.mapper.ArticleMapper;
import com.cq.RssHub.mapper.FetchTaskMapper;
import com.cq.RssHub.mapper.RssSourceMapper;
import com.cq.RssHub.pojo.Article;
import com.cq.RssHub.pojo.FetchTask;
import com.cq.RssHub.pojo.RssSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RSS服务 - 负责RSS源管理、定时抓取和任务调度
 */
@Service
public class RssService {
    private static final Logger logger = LoggerFactory.getLogger(RssService.class);
    
    @Autowired
    private RssSourceMapper rssSourceMapper;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private FetchTaskMapper fetchTaskMapper;
    
    @Autowired
    private RssParserService rssParserService;
    
    /**
     * 抓取所有活跃的RSS源
     */
    @Scheduled(fixedDelayString = "${rss.fetch.interval:3600000}")
    public void fetchAllActiveRssSources() {
        logger.info("开始抓取RSS源...");
        List<RssSource> activeSources = rssSourceMapper.findByFilters(null, null, "active");
        
        for (RssSource source : activeSources) {
            try {
                // 检查是否需要抓取，基于频率和上次抓取时间
                if (shouldFetchSource(source)) {
                    logger.info("抓取RSS源: {}", source.getName());
                    fetchAndSaveArticles(source);
                    
                    // 更新上次抓取时间
                    rssSourceMapper.updateLastFetchTime(source.getId());
                }
            } catch (Exception e) {
                logger.error("抓取RSS源失败: " + source.getName(), e);
                // 更新错误信息
                source.setErrorMessage(e.getMessage());
                source.setStatus("error");
                rssSourceMapper.update(source);
            }
        }
        logger.info("RSS源抓取完成");
    }
    
    /**
     * 立即抓取单个RSS源
     * @param sourceId RSS源ID
     * @return 新抓取的文章数量
     */
    @Transactional
    public int fetchRssSourceNow(Integer sourceId) {
        // 创建并记录抓取任务
        FetchTask task = new FetchTask();
        task.setSourceId(sourceId);
        task.setStartTime(LocalDateTime.now());
        task.setStatus("running");
        task.setArticlesAdded(0);
        
        // 保存任务
        fetchTaskMapper.insert(task);
        
        try {
            // 获取RSS源信息
            RssSource source = rssSourceMapper.findById(sourceId);
            if (source == null) {
                task.setStatus("failed");
                task.setErrorMessage("RSS源不存在");
                task.setEndTime(LocalDateTime.now());
                fetchTaskMapper.update(task);
                return 0;
            }
            
            // 抓取并保存文章
            List<Article> newArticles = fetchAndSaveArticles(source);
            
            // 更新上次抓取时间
            rssSourceMapper.updateLastFetchTime(sourceId);
            
            // 更新任务状态
            task.setStatus("completed");
            task.setArticlesAdded(newArticles.size());
            task.setEndTime(LocalDateTime.now());
            fetchTaskMapper.update(task);
            
            return newArticles.size();
        } catch (Exception e) {
            logger.error("抓取RSS源失败: " + sourceId, e);
            
            // 更新任务状态
            task.setStatus("failed");
            task.setErrorMessage(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            fetchTaskMapper.update(task);
            
            throw new RuntimeException("抓取RSS源失败", e);
        }
    }
    
    /**
     * 抓取并保存文章
     * @param rssSource RSS源
     * @return 新保存的文章列表
     */
    @Transactional
    public List<Article> fetchAndSaveArticles(RssSource rssSource) {
        List<Article> savedArticles = new ArrayList<>();
        
        try {
            // 使用RssParserService抓取文章
            List<Article> articles = rssParserService.fetchArticlesFromSource(rssSource, null);
            
            if (articles.isEmpty()) {
                logger.info("RSS源 {} 没有新文章", rssSource.getName());
                return savedArticles;
            }
            
            // 添加日志记录每篇文章的封面图
            for (Article article : articles) {
                if (article.getCoverImage() != null) {
                    logger.info("文章 [{}] 获取到封面图: {}", article.getTitle(), article.getCoverImage());
                } else {
                    logger.warn("文章 [{}] 没有封面图", article.getTitle());
                }
            }
            
            // 过滤已存在的文章（通过链接判断）
            List<String> links = articles.stream()
                    .map(Article::getLink)
                    .collect(Collectors.toList());
            
            List<String> existingLinks = articleMapper.findExistingLinks(links);
            
            List<Article> newArticles = articles.stream()
                    .filter(article -> !existingLinks.contains(article.getLink()))
                    .collect(Collectors.toList());
            
            // 保存新文章
            for (Article article : newArticles) {
                articleMapper.insert(article);
                savedArticles.add(article);
                logger.info("保存文章 [{}], 封面图: {}", article.getTitle(), article.getCoverImage());
            }
            
            logger.info("从RSS源 {} 抓取了 {} 篇新文章", rssSource.getName(), savedArticles.size());
            
            // 更新RSS源状态
            rssSource.setArticleCount(rssSource.getArticleCount() + savedArticles.size());
            rssSource.setErrorMessage(null);
            rssSource.setStatus("active");
            rssSourceMapper.update(rssSource);
            
        } catch (Exception e) {
            logger.error("处理RSS源内容失败: " + rssSource.getName(), e);
            rssSource.setErrorMessage(e.getMessage());
            rssSource.setStatus("error");
            rssSourceMapper.update(rssSource);
        }
        
        return savedArticles;
    }
    
    /**
     * 判断是否应该抓取该源
     * 基于频率和上次抓取时间
     */
    private boolean shouldFetchSource(RssSource source) {
        if (source.getLastFetchTime() == null) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastFetch = source.getLastFetchTime();
        
        // 根据频率（分钟）计算是否需要更新
        long minutesSinceLastFetch = java.time.Duration.between(lastFetch, now).toMinutes();
        return minutesSinceLastFetch >= source.getFrequency();
    }
    
    /**
     * 重新提取已存在文章的封面图
     * 可用于修复之前没有封面图的文章
     * @param sourceId 指定源ID，如果为null则处理所有文章
     * @return 更新文章数量
     */
    @Transactional
    public int reprocessArticleCoverImages(Integer sourceId) {
        logger.info("开始重新处理文章封面图，sourceId={}", sourceId);
        int updatedCount = 0;
        
        try {
            // 获取需要处理的文章列表
            List<Article> articles;
            if (sourceId != null) {
                articles = articleMapper.findByFilters(null, sourceId, null, null, null, null, null);
            } else {
                // 获取所有封面图为空的文章
                articles = articleMapper.findArticlesWithoutCoverImage();
            }
            
            logger.info("找到{}篇需要处理封面图的文章", articles.size());
            
            for (Article article : articles) {
                if (article.getContent() != null && !article.getContent().isEmpty()) {
                    // 使用RssParserService提取封面图
                    String coverImage = rssParserService.extractFirstImage(article.getContent());
                    
                    if (coverImage != null && !coverImage.isEmpty()) {
                        logger.info("为文章[{}]提取到封面图: {}", article.getTitle(), coverImage);
                        article.setCoverImage(coverImage);
                        // 更新文章
                        articleMapper.update(article);
                        updatedCount++;
                    } else {
                        logger.warn("无法为文章[{}]提取有效封面图", article.getTitle());
                    }
                }
            }
            
            logger.info("成功更新{}篇文章的封面图", updatedCount);
        } catch (Exception e) {
            logger.error("重新处理文章封面图时发生错误", e);
            throw new RuntimeException("处理封面图失败", e);
        }
        
        return updatedCount;
    }
} 