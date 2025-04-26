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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
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
        
        // 设置作者信息（增强版）
        setAuthorInfo(article, entry, source);
        
        // 设置发布日期
        if (entry.getPublishedDate() != null) {
            article.setPublishDate(convertToLocalDateTime(entry.getPublishedDate()));
        } else if (entry.getUpdatedDate() != null) {
            article.setPublishDate(convertToLocalDateTime(entry.getUpdatedDate()));
        } else {
            article.setPublishDate(LocalDateTime.now());
        }
        
        // 获取内容
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
        
        String coverImage = null;
        
        // 首先尝试从entry的各种媒体和链接中寻找封面图
        coverImage = extractFromEntryMedia(entry);
        
        // 如果没找到，再从内容中提取
        if (coverImage == null && content != null && !content.isEmpty()) {
            logger.debug("尝试从文章内容中提取封面图，文章标题: {}", entry.getTitle());
            
            // 保存原始HTML内容
            article.setContent(content);
            
            // 从内容中提取第一张图片作为封面图
            coverImage = extractFirstImage(content);
            
            // 清理HTML，设置摘要
            String summary = cleanHtml(content);
            
            // 限制长度
            if (summary.length() > 500) {
                summary = summary.substring(0, 497) + "...";
            }
            article.setSummary(summary);
        } 
        // 如果没有提供内容，但有单独的摘要
        else if (entry.getDescription() != null) {
            String description = entry.getDescription().getValue();
            
            // 如果封面图仍未找到，尝试从描述中提取
            if (coverImage == null) {
                coverImage = extractFirstImage(description);
            }
            
            // 清理HTML，只保留文本
            String summary = cleanHtml(description);
            
            // 限制长度
            if (summary.length() > 500) {
                summary = summary.substring(0, 497) + "...";
            }
            article.setSummary(summary);
        }
        
        // 确保设置封面图，如果所有尝试都失败，则使用默认占位图
        if (coverImage == null || coverImage.isEmpty()) {
            coverImage = getPlaceholderImage();
            logger.warn("未能为文章[{}]找到有效封面图，使用默认占位图", entry.getTitle());
        } else {
            logger.info("文章[{}]成功设置封面图: {}", entry.getTitle(), coverImage);
        }
        
        article.setCoverImage(coverImage);
        
        // 设置默认值
        article.setIsRead(false);
        article.setIsStarred(false);
        article.setReadCount(0);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        
        return article;
    }
    
    /**
     * 从RSS条目的媒体和链接中提取封面图
     */
    private String extractFromEntryMedia(SyndEntry entry) {
        // 1. 尝试从媒体内容中提取
        if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
            for (int i = 0; i < entry.getEnclosures().size(); i++) {
                if (entry.getEnclosures().get(i).getType() != null &&
                    entry.getEnclosures().get(i).getType().startsWith("image/")) {
                    String url = entry.getEnclosures().get(i).getUrl();
                    if (url != null && !url.isEmpty()) {
                        logger.debug("从媒体附件中提取到封面图: {}", url);
                        return url;
                    }
                }
            }
        }
        
        // 2. 尝试从RSS扩展模块中寻找媒体信息
        try {
            Object media = entry.getModule("http://search.yahoo.com/mrss/");
            if (media != null) {
                // 这里需要根据具体的RSS媒体模块实现类进行处理
                // 由于依赖不同，实现方式会有差异，这里省略具体代码
                logger.debug("发现媒体模块，但需要特定实现来处理");
            }
        } catch (Exception e) {
            logger.debug("获取媒体模块失败: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 设置文章作者信息，优先级：
     * 1. entry.getAuthor()
     * 2. entry.getAuthors()列表
     * 3. entry.getContributors()列表
     * 4. 从内容中解析byline
     * 5. RSS源名称作为兜底
     */
    private void setAuthorInfo(Article article, SyndEntry entry, RssSource source) {
        // 优先使用entry自带的author字段
        if (entry.getAuthor() != null && !entry.getAuthor().isEmpty()) {
            article.setAuthor(entry.getAuthor());
            return;
        }
        
        // 其次使用authors列表
        if (entry.getAuthors() != null && !entry.getAuthors().isEmpty()) {
            String authors = entry.getAuthors().stream()
                    .map(person -> person.getName())
                    .filter(name -> name != null && !name.isEmpty())
                    .collect(Collectors.joining(", "));
            
            if (!authors.isEmpty()) {
                article.setAuthor(authors);
                return;
            }
        }
        
        // 再次使用contributors列表
        if (entry.getContributors() != null && !entry.getContributors().isEmpty()) {
            String contributors = entry.getContributors().stream()
                    .map(person -> person.getName())
                    .filter(name -> name != null && !name.isEmpty())
                    .collect(Collectors.joining(", "));
            
            if (!contributors.isEmpty()) {
                article.setAuthor(contributors);
                return;
            }
        }
        
        // 尝试从内容中提取作者信息
        if (entry.getDescription() != null) {
            String authorFromContent = extractAuthorFromContent(entry.getDescription().getValue());
            if (authorFromContent != null && !authorFromContent.isEmpty()) {
                article.setAuthor(authorFromContent);
                return;
            }
        }
        
        // 以上方法都无法获取到作者信息时，使用RSS源名称
        article.setAuthor(source.getName());
    }
    
    /**
     * 从内容中提取作者信息
     * 本方法尝试查找常见的作者标记模式，如"By Author Name"，"作者：XXX"等
     */
    private String extractAuthorFromContent(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        // 使用Jsoup解析HTML
        Document doc = Jsoup.parse(content);
        
        // 常见的作者标记class或meta
        String[] authorSelectors = {
            "span.author", ".byline", ".author", "meta[name=author]", ".meta-author", 
            "[rel=author]", ".ArticleAuthor", ".article-author", ".entry-author"
        };
        
        for (String selector : authorSelectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                return elements.first().text().trim();
            }
        }
        
        // 尝试查找"By "或"作者："等常见模式
        String text = doc.text();
        String[] authorPatterns = {
            "By ", "by ", "作者：", "作者:", "記者", "撰文", "Author: ", "Written by "
        };
        
        for (String pattern : authorPatterns) {
            int index = text.indexOf(pattern);
            if (index >= 0) {
                // 找到模式后，提取后面的作者名
                String afterPattern = text.substring(index + pattern.length());
                // 尝试获取到下一个句号或逗号为止
                int endIndex = Math.min(
                    afterPattern.indexOf('.') != -1 ? afterPattern.indexOf('.') : Integer.MAX_VALUE,
                    afterPattern.indexOf(',') != -1 ? afterPattern.indexOf(',') : Integer.MAX_VALUE
                );
                
                if (endIndex == Integer.MAX_VALUE) {
                    endIndex = Math.min(50, afterPattern.length()); // 最多取50个字符
                }
                
                return afterPattern.substring(0, endIndex).trim();
            }
        }
        
        return null;
    }
    
    /**
     * 从HTML内容中提取第一张图片的URL
     */
    public String extractFirstImage(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return null;
        }
        
        try {
            // 首先尝试使用正则表达式提取
            String imgSrc = extractImgSrcByRegex(htmlContent);
            if (imgSrc != null && !imgSrc.isEmpty()) {
                logger.debug("通过正则表达式提取到图片: {}", imgSrc);
                return imgSrc;
            }
            
            // 然后尝试使用Jsoup提取
            Document doc = Jsoup.parse(htmlContent);
            
            // 尝试各种提取策略
            String imageUrl = tryExtractImage(doc);
            
            // 标准化URL
            if (imageUrl != null) {
                return normalizeImageUrl(imageUrl);
            }
            
            return null;
        } catch (Exception e) {
            logger.error("提取图片时出错: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 使用正则表达式提取img标签的src属性
     */
    private String extractImgSrcByRegex(String html) {
        if (html == null || html.isEmpty()) {
            return null;
        }
        
        try {
            // 更强大的正则表达式，处理各种img标签格式
            // 匹配: <img src="URL" ... /> 或 <img src='URL' ... /> 或 <img src=URL ... />
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "<img[^>]+src\\s*=\\s*['\"]?([^'\"\\s>]+)['\"]?[^>]*>",
                java.util.regex.Pattern.CASE_INSENSITIVE
            );
            
            java.util.regex.Matcher matcher = pattern.matcher(html);
            
            if (matcher.find()) {
                String src = matcher.group(1).trim();
                
                // 过滤掉可能的SVG、图标等
                if (!isSmallIcon(src)) {
                    logger.debug("通过正则找到图片URL: {}", src);
                    return src;
                }
            }
            
            // 尝试查找背景图片
            pattern = java.util.regex.Pattern.compile(
                "background-image\\s*:\\s*url\\(['\"]?([^'\"\\)]+)['\"]?\\)",
                java.util.regex.Pattern.CASE_INSENSITIVE
            );
            
            matcher = pattern.matcher(html);
            if (matcher.find()) {
                String src = matcher.group(1).trim();
                logger.debug("找到背景图片URL: {}", src);
                return src;
            }
            
            return null;
        } catch (Exception e) {
            logger.error("正则提取图片URL时出错: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 尝试多种方式提取图片
     */
    private String tryExtractImage(Document doc) {
        String imgUrl = null;
        
        // 尝试提取大尺寸图片 (优先提取宽高最大的图片)
        imgUrl = extractLargestImage(doc);
        if (imgUrl != null) {
            return normalizeImageUrl(imgUrl);
        }
        
        // 尝试提取第一个非小图标图片
        Elements imgElements = doc.select("img");
        for (Element img : imgElements) {
            String src = img.attr("src");
            // 增加对srcset属性的检查
            if (src.isEmpty()) {
                String srcset = img.attr("srcset");
                if (!srcset.isEmpty()) {
                    // 从srcset中提取第一个URL (格式通常是"url 1x, url 2x")
                    src = srcset.split("\\s+")[0];
                }
            }
            
            if (!src.isEmpty() && !isSmallIcon(src)) {
                logger.debug("找到非小图标图片: {}", src);
                return normalizeImageUrl(src);
            }
        }
        
        // 尝试提取背景图片
        Elements elementsWithBg = doc.select("[style*=background-image]");
        for (Element el : elementsWithBg) {
            String style = el.attr("style");
            if (style.contains("background-image")) {
                java.util.regex.Pattern urlPattern = java.util.regex.Pattern.compile("background-image:\\s*url\\(['\"]?(.*?)['\"]?\\)");
                java.util.regex.Matcher matcher = urlPattern.matcher(style);
                if (matcher.find()) {
                    String bgUrl = matcher.group(1);
                    if (!isSmallIcon(bgUrl)) {
                        logger.debug("找到背景图片: {}", bgUrl);
                        return normalizeImageUrl(bgUrl);
                    }
                }
            }
        }
        
        // 尝试提取Open Graph和Twitter Card图片
        imgUrl = extractMetaImage(doc);
        if (imgUrl != null) {
            return normalizeImageUrl(imgUrl);
        }
        
        // 尝试从文章主体内容提取图片
        Elements articleElements = doc.select("article, .article, .content, .post, main");
        if (!articleElements.isEmpty()) {
            for (Element article : articleElements) {
                Elements articleImages = article.select("img");
                for (Element img : articleImages) {
                    String src = img.attr("src");
                    if (!src.isEmpty() && !isSmallIcon(src)) {
                        logger.debug("从文章主体找到图片: {}", src);
                        return normalizeImageUrl(src);
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 尝试提取最大尺寸的图片
     */
    private String extractLargestImage(Document doc) {
        Elements images = doc.select("img");
        int maxSize = 0;
        String largestImageUrl = null;
        
        for (Element img : images) {
            String width = img.attr("width");
            String height = img.attr("height");
            String src = img.attr("src");
            
            // 跳过没有src的图片
            if (src.isEmpty()) continue;
            
            // 尝试从width和height属性判断图片大小
            int imgWidth = 0;
            int imgHeight = 0;
            
            try {
                if (!width.isEmpty()) imgWidth = Integer.parseInt(width.replaceAll("[^0-9]", ""));
                if (!height.isEmpty()) imgHeight = Integer.parseInt(height.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                // 忽略无法解析的数值
            }
            
            int size = imgWidth * imgHeight;
            
            // 如果无法从属性获取尺寸，检查样式
            if (size == 0) {
                String style = img.attr("style");
                if (style.contains("width") || style.contains("height")) {
                    java.util.regex.Pattern widthPattern = java.util.regex.Pattern.compile("width:\\s*(\\d+)px");
                    java.util.regex.Pattern heightPattern = java.util.regex.Pattern.compile("height:\\s*(\\d+)px");
                    
                    java.util.regex.Matcher widthMatcher = widthPattern.matcher(style);
                    java.util.regex.Matcher heightMatcher = heightPattern.matcher(style);
                    
                    if (widthMatcher.find()) {
                        imgWidth = Integer.parseInt(widthMatcher.group(1));
                    }
                    
                    if (heightMatcher.find()) {
                        imgHeight = Integer.parseInt(heightMatcher.group(1));
                    }
                    
                    size = imgWidth * imgHeight;
                }
            }
            
            // 如果尺寸足够大且不是小图标
            if (size > maxSize && !isSmallIcon(src)) {
                maxSize = size;
                largestImageUrl = src;
            }
        }
        
        if (largestImageUrl != null) {
            logger.debug("找到最大尺寸图片: {}，尺寸: {}", largestImageUrl, maxSize);
        }
        
        return largestImageUrl;
    }
    
    /**
     * 尝试提取<meta>标签中的图片URL
     */
    private String extractMetaImage(Document doc) {
        // 尝试Open Graph图片
        String ogImage = doc.select("meta[property=og:image]").attr("content");
        if (!ogImage.isEmpty()) {
            logger.debug("找到Open Graph图片: {}", ogImage);
            return ogImage;
        }
        
        // 尝试Twitter Card图片
        String twitterImage = doc.select("meta[name=twitter:image]").attr("content");
        if (!twitterImage.isEmpty()) {
            logger.debug("找到Twitter Card图片: {}", twitterImage);
            return twitterImage;
        }
        
        // 尝试文章图片相关meta标签
        String articleImage = doc.select("meta[property=article:image], meta[name=thumbnail]").attr("content");
        if (!articleImage.isEmpty()) {
            logger.debug("找到文章图片meta标签: {}", articleImage);
            return articleImage;
        }
        
        return null;
    }
    
    /**
     * 判断是否是小图标
     */
    private boolean isSmallIcon(String imgUrl) {
        if (imgUrl == null) return true;
        
        // 宽松条件：只过滤明显的图标和1x1像素图
        return imgUrl.contains("1x1.gif") || 
               imgUrl.contains("spacer.gif") ||
               imgUrl.contains("blank.gif") ||
               imgUrl.contains("pixel.gif");
    }
    
    /**
     * 标准化图片URL
     */
    private String normalizeImageUrl(String imgUrl) {
        if (imgUrl == null || imgUrl.isEmpty()) {
            return null;
        }
        
        try {
            // 去除URL前后空白
            imgUrl = imgUrl.trim();
            
            // 处理相对URL
            if (imgUrl.startsWith("//")) {
                return "https:" + imgUrl;
            } else if (imgUrl.startsWith("/")) {
                // 相对路径需要域名，此处返回null后可由调用者处理
                return null;
            }
            
            // 检查URL是否有效
            if (!imgUrl.startsWith("http://") && !imgUrl.startsWith("https://") && !imgUrl.startsWith("data:")) {
                // 尝试添加https前缀
                imgUrl = "https://" + imgUrl;
            }
            
            // 移除图片URL中的转义字符
            imgUrl = imgUrl.replace("&amp;", "&");
            
            return imgUrl;
        } catch (Exception e) {
            logger.error("标准化图片URL时出错: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取一个占位图
     */
    private String getPlaceholderImage() {
        // 返回一个默认的占位图URL
        return "https://via.placeholder.com/300x200?text=No+Image";
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
