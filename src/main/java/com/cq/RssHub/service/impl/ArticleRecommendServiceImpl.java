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
        // 中文停用词
        "的", "了", "和", "与", "这", "那", "是", "在", "有", "为", "啊", "吗", "么", "嗯", "呢", "吧",
        "对", "我", "你", "他", "她", "它", "们", "个", "之", "而", "以", "及", "等", "也", "就", "但",
        "都", "又", "将", "把", "能", "可以", "使", "让", "被", "所", "如", "要", "想", "会", "得", "到",
        "从", "上", "下", "前", "后", "里", "外", "内", "中", "间", "很", "更", "最", "非常", "越", "于",
        "或", "某", "某些", "这些", "那些", "每", "各", "各种", "一些", "只是", "因为", "如果", "虽然", "一个",
        "这个", "这样", "那样", "什么", "哪", "哪些", "如何", "怎么", "怎样", "一", "二", "三", "四", "五",
        // 英文停用词
        "the", "a", "an", "of", "to", "and", "in", "for", "on", "with", "by", "at", "is", "are", "was", "were",
        "this", "that", "these", "those", "it", "its", "they", "them", "their", "we", "our", "us", "i", "my", "me",
        "you", "your", "he", "him", "his", "she", "her", "who", "what", "which", "where", "when", "why", "how",
        "all", "any", "both", "each", "more", "most", "other", "some", "such", "no", "not", "only", "than",
        "too", "very", "can", "will", "just", "should", "now", "also", "as", "be", "been", "but", "had", "has",
        "have", "if", "or", "because", "while", "about", "up", "down", "out", "then", "so"
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
        if (textTerms.isEmpty()) return 0.0;
        
        // 检查是否有关键词匹配 - 直接检查整个文本是否包含特定关键词
        double keywordMatchBoost = 0.0;
        for (String term : profile.keySet()) {
            // 对于长度>=2的词语，如果在文本中找到，增加基础分
            if (term.length() >= 2 && text.contains(term)) {
                keywordMatchBoost += 0.1 * profile.get(term);
            }
        }
        
        // 计算余弦相似度
        double dotProduct = 0.0;
        for (Map.Entry<String, Double> entry : profile.entrySet()) {
            String term = entry.getKey();
            if (textTerms.containsKey(term)) {
                dotProduct += entry.getValue() * textTerms.get(term);
            }
        }
        
        // 如果没有共同词语，但有关键词匹配，返回提升后的相似度
        if (dotProduct == 0.0 && keywordMatchBoost > 0.0) {
            return keywordMatchBoost; // 返回基于关键词匹配的相似度
        }
        
        // 如果没有共同词语和关键词匹配，相似度为0
        if (dotProduct == 0.0) return 0.0;
        
        // 计算向量长度
        double profileNorm = Math.sqrt(profile.values().stream().mapToDouble(v -> v * v).sum());
        double textNorm = Math.sqrt(textTerms.values().stream().mapToDouble(v -> v * v).sum());
        
        // 余弦相似度
        double cosineSimilarity = dotProduct / (profileNorm * textNorm);
        
        // 添加关键词匹配提升
        return cosineSimilarity + keywordMatchBoost;
    }
    
    /**
     * 从文本中提取词语并计算权重
     */
    private Map<String, Double> extractTermsWithWeight(String text) {
        if (text == null || text.isEmpty()) return Map.of();
        
        Map<String, Double> terms = new HashMap<>();
        
        // 改进的中英文混合分词
        String preprocessedText = text.toLowerCase()
                .replaceAll("[\\p{P}\\p{S}]", " "); // 去除标点和符号
        
        // 处理英文词语 - 按空格分割
        String[] words = preprocessedText.split("\\s+");
        for (String word : words) {
            if (word.length() < 2 || STOP_WORDS.contains(word)) continue;
            
            terms.put(word, terms.getOrDefault(word, 0.0) + 1.0);
        }
        
        // 特殊处理中文 - 简单的N-gram方法处理连续中文字符
        // 提取2-gram和3-gram中文短语
        String chineseOnly = preprocessedText.replaceAll("[^\\u4e00-\\u9fa5]", ""); // 仅保留中文字符
        
        // 提取2-gram中文词组（连续两个汉字可能是一个词）
        if (chineseOnly.length() >= 2) {
            for (int i = 0; i < chineseOnly.length() - 1; i++) {
                String bigram = chineseOnly.substring(i, i + 2);
                if (!STOP_WORDS.contains(bigram)) {
                    terms.put(bigram, terms.getOrDefault(bigram, 0.0) + 0.5); // 权重略低
                }
            }
        }
        
        // 提取3-gram中文词组（连续三个汉字可能是一个词）
        if (chineseOnly.length() >= 3) {
            for (int i = 0; i < chineseOnly.length() - 2; i++) {
                String trigram = chineseOnly.substring(i, i + 3);
                if (!STOP_WORDS.contains(trigram)) {
                    terms.put(trigram, terms.getOrDefault(trigram, 0.0) + 0.8); // 较高权重
                }
            }
        }
        
        // 处理常见的中文关键词（技术、主题词等）
        String[] commonKeywords = {"科技", "人工智能", "机器学习", "深度学习", "算法", "编程", 
                                  "旅游", "美食", "风景", "攻略", "推荐"};
        
        for (String keyword : commonKeywords) {
            if (text.contains(keyword) && !STOP_WORDS.contains(keyword)) {
                // 给予常见关键词额外权重
                terms.put(keyword, terms.getOrDefault(keyword, 0.0) + 1.5);
            }
        }
        
        // 简单的TF计算
        double totalTerms = terms.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalTerms > 0) {  // 避免除以零
            for (Map.Entry<String, Double> entry : terms.entrySet()) {
                terms.put(entry.getKey(), entry.getValue() / totalTerms);
            }
        }
        
        return terms;
    }
} 