package com.cq.RssHub.service.impl;

import com.cq.RssHub.mapper.ArticleMapper;
import com.cq.RssHub.pojo.Article;
import com.cq.RssHub.pojo.ArticleFavorite;
import com.cq.RssHub.service.ArticleFavoriteService;
import com.cq.RssHub.service.ArticleRecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章推荐服务实现类
 * 使用TF-IDF算法实现基于内容的文章推荐
 */
@Service
public class ArticleRecommendServiceImpl implements ArticleRecommendService {
    private static final Logger logger = LoggerFactory.getLogger(ArticleRecommendServiceImpl.class);
    
    @Autowired
    private ArticleFavoriteService articleFavoriteService;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    /**
     * 停用词集合
     */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "了", "和", "与", "这", "那", "是", "在", "有", "为", "啊", "吗", "么", "嗯", "呢", "吧",
        "the", "a", "an", "of", "to", "and", "in", "for", "on", "with", "by", "at", "is", "are", "was", "were"
    ));
    
    @Override
    @Cacheable(value = "personalizedRecommendations", key = "#userId + '-' + #limit", unless = "#result.isEmpty()")
    public List<Article> getPersonalizedRecommendations(Integer userId, int limit) {
        try {
            // 获取用户收藏列表
            List<ArticleFavorite> favorites = articleFavoriteService.getRecentFavorites(userId, 10);
            
            // 如果没有收藏，返回热门推荐
            if (favorites.isEmpty()) {
                logger.info("用户{}没有收藏记录，返回热门推荐", userId);
                return getHotRecommendations(limit);
            }
            
            // 提取收藏文章的标题
            List<String> favoriteTitles = favorites.stream()
                    .map(ArticleFavorite::getArticleTitle)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            // 计算TF-IDF特征
            Map<Integer, Double> articleScores = calculateArticleScores(favoriteTitles);
            
            // 排除已收藏的文章
            Set<Integer> favoriteIds = favorites.stream()
                    .map(ArticleFavorite::getArticleId)
                    .collect(Collectors.toSet());
            
            // 按相似度排序并限制数量
            List<Integer> recommendArticleIds = articleScores.entrySet().stream()
                    .filter(entry -> !favoriteIds.contains(entry.getKey()))
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            // 获取推荐文章列表
            if (!recommendArticleIds.isEmpty()) {
                List<Article> recommendations = new ArrayList<>();
                for (Integer articleId : recommendArticleIds) {
                    Article article = articleMapper.findById(articleId);
                    if (article != null) {
                        recommendations.add(article);
                    }
                }
                
                logger.info("为用户{}生成了{}篇个性化推荐文章", userId, recommendations.size());
                return recommendations;
            }
            
            // 如果没有匹配到合适的推荐，返回热门推荐
            logger.info("未能为用户{}找到个性化推荐文章，返回热门推荐", userId);
            return getHotRecommendations(limit);
        } catch (Exception e) {
            logger.error("生成个性化推荐失败", e);
            return getHotRecommendations(limit);
        }
    }
    
    @Override
    @Cacheable(value = "similarArticles", key = "#articleId + '-' + #limit", unless = "#result.isEmpty()")
    public List<Article> getSimilarArticles(Integer articleId, int limit) {
        try {
            // 获取当前文章
            Article article = articleMapper.findById(articleId);
            if (article == null || article.getTitle() == null) {
                logger.warn("文章{}不存在或标题为空", articleId);
                return List.of();
            }
            
            // 提取当前文章标题词语
            Map<String, Double> titleTerms = extractTermsWithWeight(article.getTitle());
            
            // 获取最新文章
            List<Article> latestArticles = articleMapper.findLatestArticles(100); // 获取较多文章以便筛选
            
            // 计算相似度
            Map<Integer, Double> similarityScores = new HashMap<>();
            for (Article candidate : latestArticles) {
                // 排除自身
                if (candidate.getId().equals(articleId)) {
                    continue;
                }
                
                // 计算标题相似度
                double similarity = calculateSimilarity(titleTerms, candidate.getTitle());
                similarityScores.put(candidate.getId(), similarity);
            }
            
            // 按相似度排序并限制数量
            List<Integer> similarArticleIds = similarityScores.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            // 获取相似文章列表
            List<Article> similarArticles = new ArrayList<>();
            for (Integer id : similarArticleIds) {
                for (Article latestArticle : latestArticles) {
                    if (latestArticle.getId().equals(id)) {
                        similarArticles.add(latestArticle);
                        break;
                    }
                }
            }
            
            logger.info("为文章{}找到了{}篇相似文章", articleId, similarArticles.size());
            return similarArticles;
        } catch (Exception e) {
            logger.error("查找相似文章失败", e);
            return List.of();
        }
    }
    
    @Override
    @Cacheable(value = "hotRecommendations", key = "#limit", unless = "#result.isEmpty()")
    public List<Article> getHotRecommendations(int limit) {
        try {
            // 简单实现：返回阅读量最高的文章
            List<Article> hotArticles = articleMapper.findLatestArticles(limit);
            logger.info("获取了{}篇热门推荐文章", hotArticles.size());
            return hotArticles;
        } catch (Exception e) {
            logger.error("获取热门推荐失败", e);
            return List.of();
        }
    }
    
    /**
     * 计算文章相似度评分
     */
    private Map<Integer, Double> calculateArticleScores(List<String> favoriteTitles) {
        // 提取用户收藏文章的关键词权重
        Map<String, Double> userProfile = buildUserProfile(favoriteTitles);
        
        // 获取最新文章用于推荐
        List<Article> candidates = articleMapper.findLatestArticles(100);
        
        // 计算每篇文章与用户兴趣的相似度
        Map<Integer, Double> articleScores = new HashMap<>();
        for (Article article : candidates) {
            if (article.getTitle() == null) continue;
            
            double score = calculateSimilarity(userProfile, article.getTitle());
            articleScores.put(article.getId(), score);
        }
        
        return articleScores;
    }
    
    /**
     * 构建用户兴趣档案（基于收藏文章的词频）
     */
    private Map<String, Double> buildUserProfile(List<String> titles) {
        Map<String, Double> profile = new HashMap<>();
        
        // 统计所有收藏文章标题中的词频
        for (String title : titles) {
            if (title == null) continue;
            
            Map<String, Double> terms = extractTermsWithWeight(title);
            for (Map.Entry<String, Double> entry : terms.entrySet()) {
                String term = entry.getKey();
                Double weight = entry.getValue();
                profile.put(term, profile.getOrDefault(term, 0.0) + weight);
            }
        }
        
        return profile;
    }
    
    /**
     * 计算文本相似度
     */
    private double calculateSimilarity(Map<String, Double> profile, String text) {
        if (text == null || profile.isEmpty()) return 0.0;
        
        Map<String, Double> textTerms = extractTermsWithWeight(text);
        
        // 计算余弦相似度
        double dotProduct = 0.0;
        for (Map.Entry<String, Double> entry : profile.entrySet()) {
            String term = entry.getKey();
            if (textTerms.containsKey(term)) {
                dotProduct += entry.getValue() * textTerms.get(term);
            }
        }
        
        // 如果没有共同词语，相似度为0
        if (dotProduct == 0.0) return 0.0;
        
        // 计算向量长度
        double profileNorm = Math.sqrt(profile.values().stream().mapToDouble(v -> v * v).sum());
        double textNorm = Math.sqrt(textTerms.values().stream().mapToDouble(v -> v * v).sum());
        
        // 余弦相似度
        return dotProduct / (profileNorm * textNorm);
    }
    
    /**
     * 从文本中提取词语并计算权重
     */
    private Map<String, Double> extractTermsWithWeight(String text) {
        if (text == null || text.isEmpty()) return Map.of();
        
        Map<String, Double> terms = new HashMap<>();
        
        // 简单分词（中英文混合）
        String[] words = text.toLowerCase()
                .replaceAll("[\\p{P}\\p{S}]", " ") // 去除标点和符号
                .split("\\s+");
        
        // 计算词频
        for (String word : words) {
            if (word.length() < 2 || STOP_WORDS.contains(word)) continue;
            
            terms.put(word, terms.getOrDefault(word, 0.0) + 1.0);
        }
        
        // 简单的TF计算
        double totalTerms = terms.values().stream().mapToDouble(Double::doubleValue).sum();
        for (Map.Entry<String, Double> entry : terms.entrySet()) {
            terms.put(entry.getKey(), entry.getValue() / totalTerms);
        }
        
        return terms;
    }
} 