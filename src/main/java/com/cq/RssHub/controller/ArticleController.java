package com.cq.RssHub.controller;

import com.cq.RssHub.pojo.Article;
import com.cq.RssHub.pojo.ResponseMessage;
import com.cq.RssHub.pojo.dto.BatchIdsDTO;
import com.cq.RssHub.pojo.vo.PageArticleVO;
import com.cq.RssHub.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rss/articles")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @GetMapping
    public ResponseMessage<?> getArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer sourceId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        
        PageArticleVO pageArticle = articleService.getArticles(
                keyword, sourceId, categoryId, startDate, endDate, page, pageSize);
        
        return ResponseMessage.success("获取文章列表成功", pageArticle);
    }

    @GetMapping("/{id}")
    public ResponseMessage<?> getArticleById(@PathVariable Integer id) {
        Article article = articleService.getArticleById(id);
        if (article != null) {
            // 增加阅读计数
            articleService.incrementReadCount(id);
            return ResponseMessage.success("获取文章详情成功", article);
        } else {
            return ResponseMessage.error("文章不存在");
        }
    }

    @GetMapping("/latest")
    public ResponseMessage<?> getLatestArticles(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Article> latestArticles = articleService.getLatestArticles(limit);
        return ResponseMessage.success("获取最新文章成功", latestArticles);
    }

    @PostMapping
    public ResponseMessage<?> createArticle(@RequestBody Article article) {
        Article createdArticle = articleService.createArticle(article);
        if (createdArticle != null) {
            return ResponseMessage.success("创建文章成功", createdArticle);
        } else {
            return ResponseMessage.error("创建文章失败");
        }
    }

    @PutMapping("/{id}")
    public ResponseMessage<?> updateArticle(
            @PathVariable Integer id,
            @RequestBody Article article) {
        
        article.setId(id);
        boolean updated = articleService.updateArticle(article);
        
        if (updated) {
            return ResponseMessage.success("更新文章成功");
        } else {
            return ResponseMessage.error("更新文章失败");
        }
    }

    @PutMapping("/{id}/read")
    public ResponseMessage<?> markAsRead(@PathVariable Integer id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return ResponseMessage.error("文章不存在");
        }
        
        article.setIsRead(true);
        boolean updated = articleService.updateArticle(article);
        
        if (updated) {
            return ResponseMessage.success("标记为已读成功");
        } else {
            return ResponseMessage.error("标记为已读失败");
        }
    }

    @PutMapping("/{id}/unread")
    public ResponseMessage<?> markAsUnread(@PathVariable Integer id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return ResponseMessage.error("文章不存在");
        }
        
        article.setIsRead(false);
        boolean updated = articleService.updateArticle(article);
        
        if (updated) {
            return ResponseMessage.success("标记为未读成功");
        } else {
            return ResponseMessage.error("标记为未读失败");
        }
    }

    @PutMapping("/{id}/star")
    public ResponseMessage<?> markAsStarred(@PathVariable Integer id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return ResponseMessage.error("文章不存在");
        }
        
        article.setIsStarred(true);
        boolean updated = articleService.updateArticle(article);
        
        if (updated) {
            return ResponseMessage.success("标记为收藏成功");
        } else {
            return ResponseMessage.error("标记为收藏失败");
        }
    }

    @PutMapping("/{id}/unstar")
    public ResponseMessage<?> unmarkAsStarred(@PathVariable Integer id) {
        Article article = articleService.getArticleById(id);
        if (article == null) {
            return ResponseMessage.error("文章不存在");
        }
        
        article.setIsStarred(false);
        boolean updated = articleService.updateArticle(article);
        
        if (updated) {
            return ResponseMessage.success("取消收藏成功");
        } else {
            return ResponseMessage.error("取消收藏失败");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseMessage<?> deleteArticle(@PathVariable Integer id) {
        boolean deleted = articleService.deleteArticle(id);
        if (deleted) {
            return ResponseMessage.success("删除文章成功");
        } else {
            return ResponseMessage.error("删除文章失败");
        }
    }

    @DeleteMapping("/batch")
    public ResponseMessage<?> deleteArticles(@RequestBody BatchIdsDTO batchIds) {
        boolean deleted = articleService.deleteArticles(batchIds.getIds());
        if (deleted) {
            return ResponseMessage.success("批量删除文章成功");
        } else {
            return ResponseMessage.error("批量删除文章失败");
        }
    }
} 