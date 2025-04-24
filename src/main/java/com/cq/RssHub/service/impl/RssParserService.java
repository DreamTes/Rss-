package com.cq.RssHub.service.impl;

import com.cq.RssHub.pojo.Article;
import com.cq.RssHub.pojo.RssSource;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 提供统一的RSS解析和转换服务
 * 负责RSS源的解析、内容提取和数据转换，不处理业务逻辑和持久化
 */
@Service
public class RssParserService {
    private static final Logger logger = LoggerFactory.getLogger(RssParserService.class);
    
    // 用于异步处理的线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    // HTTP客户端，可重用
    private final HttpClient httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    
    /**
     * 解析RSS URL
     * @param url RSS源URL
     * @return 解析后的SyndFeed对象
     * @throws Exception 解析异常
     */
    public SyndFeed parseFeed(String url) throws Exception {
        logger.info("开始解析RSS源: {}", url);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("User-Agent", "RssHub/1.0")
                .GET()
                .build();
            
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            
            // 检查响应状态
            if (response.statusCode() != 200) {
                throw new IOException("HTTP错误: " + response.statusCode());
            }
            
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(response.body()));
            
            logger.info("成功解析RSS源: {}, 文章数: {}", url, feed.getEntries().size());
            return feed;
        } catch (URISyntaxException e) {
            logger.error("URL格式错误: {}", url, e);
            throw e;
        } catch (IOException e) {
            logger.error("网络错误: {}", url, e);
            throw e;
        } catch (FeedException e) {
            logger.error("RSS解析错误: {}", url, e);
            throw e;
        } catch (Exception e) {
            logger.error("未知错误: {}", url, e);
            throw e;
        }
    }
    
    /**
     * 异步解析RSS URL
     * @param url RSS源URL
     * @return 包含解析结果的CompletableFuture
     */
    public CompletableFuture<SyndFeed> parseFeedAsync(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return parseFeed(url);
            } catch (Exception e) {
                logger.error("异步解析RSS源失败: {}", url, e);
                throw new RuntimeException("解析失败: " + e.getMessage(), e);
            }
        }, executorService);
    }
    
    /**
     * 将SyndEntry转换为Article
     * @param entry RSS条目
     * @param source RSS源信息
     * @return 转换后的Article对象
     */
    public Article convertToArticle(SyndEntry entry, RssSource source) {
        Article article = new Article();
        
        // 设置基本信息
        article.setTitle(entry.getTitle());
        article.setLink(entry.getLink());
        article.setSourceId(source.getId());
        
        // 设置作者信息
        if (entry.getAuthor() != null && !entry.getAuthor().isEmpty()) {
            article.setAuthor(entry.getAuthor());
        } else if (entry.getAuthors() != null && !entry.getAuthors().isEmpty()) {
            article.setAuthor(entry.getAuthors().get(0).getName());
        } else {
            article.setAuthor(source.getName());
        }
        
        // 设置发布日期
        if (entry.getPublishedDate() != null) {
            article.setPublishDate(convertToLocalDateTime(entry.getPublishedDate()));
        } else if (entry.getUpdatedDate() != null) {
            article.setPublishDate(convertToLocalDateTime(entry.getUpdatedDate()));
        } else {
            article.setPublishDate(LocalDateTime.now());
        }
        
        // 设置摘要
        if (entry.getDescription() != null) {
            // 清理HTML，只保留文本
            String summary = cleanHtml(entry.getDescription().getValue());
            
            // 限制长度
            if (summary.length() > 500) {
                summary = summary.substring(0, 497) + "...";
            }
            article.setSummary(summary);
        }
        
        // 设置内容
        String content = null;
        
        // 先尝试获取完整内容
        if (!entry.getContents().isEmpty()) {
            SyndContent syndContent = entry.getContents().get(0);
            content = syndContent.getValue();
        } 
        // 如果没有内容，使用描述作为内容
        else if (entry.getDescription() != null) {
            content = entry.getDescription().getValue();
        }
        
        if (content != null) {
            // 保存原始HTML内容
            article.setContent(content);
        }
        
        // 设置默认值
        article.setIsRead(false);
        article.setIsStarred(false);
        article.setReadCount(0);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        
        return article;
    }
    
    /**
     * 从RSS源获取文章列表
     * @param source RSS源
     * @param maxArticles 最大文章数量，如果为null则获取全部
     * @return 文章列表
     * @throws Exception 解析异常
     */
    public List<Article> fetchArticlesFromSource(RssSource source, Integer maxArticles) throws Exception {
        SyndFeed feed = parseFeed(source.getUrl());
        List<Article> articles = new ArrayList<>();
        
        List<SyndEntry> entries = feed.getEntries();
        
        // 如果指定了最大文章数，则限制获取数量
        if (maxArticles != null && maxArticles > 0 && maxArticles < entries.size()) {
            entries = entries.subList(0, maxArticles);
        }
        
        for (SyndEntry entry : entries) {
            Article article = convertToArticle(entry, source);
            articles.add(article);
        }
        
        return articles;
    }
    
    /**
     * 异步从多个RSS源获取文章
     * @param sources RSS源列表
     * @param maxArticlesPerSource 每个源最大获取的文章数量
     * @return 所有文章列表的CompletableFuture
     */
    public CompletableFuture<List<Article>> fetchArticlesFromSourcesAsync(List<RssSource> sources, Integer maxArticlesPerSource) {
        List<CompletableFuture<List<Article>>> futures = sources.stream()
            .map(source -> CompletableFuture.supplyAsync(() -> {
                try {
                    return fetchArticlesFromSource(source, maxArticlesPerSource);
                } catch (Exception e) {
                    logger.error("获取源文章失败: {}", source.getUrl(), e);
                    return List.<Article>of();
                }
            }, executorService))
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }
    
    /**
     * 将Date转换为LocalDateTime
     */
    public LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }
    
    /**
     * 清理HTML内容，只保留基本格式
     */
    public String cleanHtml(String html) {
        if (html == null) {
            return "";
        }
        
        // 使用Jsoup清理HTML，只保留基本标签
        return Jsoup.clean(html, Safelist.basic());
    }
    
    /**
     * 提取纯文本内容（移除所有HTML标签）
     */
    public String extractText(String html) {
        if (html == null) {
            return "";
        }
        
        // 完全移除HTML标签
        return Jsoup.parse(html).text();
    }
    
    /**
     * 检查文章是否在指定的时间范围内
     * @param publishDate 文章发布日期
     * @param days 天数范围
     * @return 是否在范围内
     */
    public boolean isWithinTimeRange(LocalDateTime publishDate, int days) {
        if (publishDate == null) {
            return true;
        }
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return publishDate.isAfter(cutoffDate);
    }
}
