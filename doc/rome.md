# Rome 库使用文档

## 1. Rome 库简介

Rome 是一个强大的 Java 库，用于处理各种格式的 RSS 和 Atom 订阅源。它支持所有主流的 RSS 版本（0.90, 0.91, 0.92, 0.93, 0.94, 1.0, 2.0）以及 Atom 0.3 和 Atom 1.0 格式。

### 主要特点

- **格式统一化**：将所有格式的订阅源转换为统一的 `SyndFeed` 对象模型
- **多格式支持**：轻松处理不同版本的 RSS 和 Atom
- **扩展性**：支持标准和自定义模块扩展
- **简单易用**：提供简洁直观的 API

### Maven 依赖

```xml
<dependency>
    <groupId>com.rometools</groupId>
    <artifactId>rome</artifactId>
    <version>1.18.0</version>
</dependency>
```

## 2. 解析 RSS/Atom 源

### 2.1 基本解析步骤

1. 创建一个 `SyndFeedInput` 对象
2. 使用 `XmlReader` 读取 Feed URL 或数据流
3. 调用 `build()` 方法解析 Feed
4. 通过 `SyndFeed` 对象访问 Feed 信息

### 2.2 代码示例

```java
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.util.List;

public class RssParser {
    public SyndFeed parseFeed(String feedUrl) throws Exception {
        URL url = new URL(feedUrl);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(url));
        return feed;
    }
    
    public void displayFeedInfo(SyndFeed feed) {
        // 获取 Feed 基本信息
        System.out.println("Feed 标题: " + feed.getTitle());
        System.out.println("Feed 链接: " + feed.getLink());
        System.out.println("Feed 描述: " + feed.getDescription());
        System.out.println("Feed 类型: " + feed.getFeedType());
        System.out.println("Feed 语言: " + feed.getLanguage());
        
        // 获取文章列表
        List<SyndEntry> entries = feed.getEntries();
        System.out.println("文章数量: " + entries.size());
        
        // 遍历文章
        for (SyndEntry entry : entries) {
            System.out.println("\n标题: " + entry.getTitle());
            System.out.println("链接: " + entry.getLink());
            System.out.println("作者: " + entry.getAuthor());
            System.out.println("发布日期: " + entry.getPublishedDate());
            
            // 获取摘要
            if (entry.getDescription() != null) {
                System.out.println("摘要: " + entry.getDescription().getValue());
            }
            
            // 获取完整内容
            if (!entry.getContents().isEmpty()) {
                System.out.println("完整内容: " + entry.getContents().get(0).getValue());
            }
        }
    }
}
```

### 2.3 使用 HTTP 客户端获取 Feed

对于需要处理重定向、设置请求头或处理代理的情况，使用 HTTP 客户端更为灵活：

```java
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public SyndFeed parseFeedWithHttpClient(String feedUrl) throws Exception {
    HttpClient client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(feedUrl))
        .header("User-Agent", "Mozilla/5.0 RSSReader")
        .GET()
        .build();
    
    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
    
    SyndFeedInput input = new SyndFeedInput();
    return input.build(new XmlReader(response.body()));
}
```

## 3. 处理 Feed 内容

### 3.1 SyndFeed 核心属性

| 属性          | 描述                   | 方法                              |
|--------------|------------------------|----------------------------------|
| 标题         | Feed 的标题            | `feed.getTitle()`                |
| 链接         | Feed 的网站链接        | `feed.getLink()`                 |
| 描述         | Feed 的描述信息        | `feed.getDescription()`          |
| 发布日期     | Feed 的发布日期        | `feed.getPublishedDate()`        |
| 作者         | Feed 的作者            | `feed.getAuthor()`               |
| 文章列表     | Feed 包含的所有条目    | `feed.getEntries()`              |
| Feed 类型    | Feed 的格式类型        | `feed.getFeedType()`             |
| 图片         | Feed 的图片            | `feed.getImage()`                |

### 3.2 SyndEntry 核心属性

| 属性          | 描述                   | 方法                              |
|--------------|------------------------|----------------------------------|
| 标题         | 文章的标题             | `entry.getTitle()`               |
| 链接         | 文章的链接             | `entry.getLink()`                |
| 作者         | 文章的作者             | `entry.getAuthor()`              |
| 发布日期     | 文章的发布日期         | `entry.getPublishedDate()`       |
| 更新日期     | 文章的更新日期         | `entry.getUpdatedDate()`         |
| 描述         | 文章的摘要描述         | `entry.getDescription().getValue()` |
| 内容         | 文章的完整内容         | `entry.getContents().get(0).getValue()` |
| 分类         | 文章的分类标签         | `entry.getCategories()`          |
| 附件         | 文章的附件             | `entry.getEnclosures()`          |

## 4. 创建 RSS Feed

### 4.1 基本创建步骤

1. 创建 `SyndFeed` 实例
2. 设置 Feed 的属性（标题、链接、描述等）
3. 创建并添加 `SyndEntry` 条目
4. 使用 `SyndFeedOutput` 将 Feed 输出为 XML

### 4.2 代码示例

```java
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.SyndFeedOutput;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RssGenerator {
    public SyndFeed createFeed() {
        // 创建 Feed
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0"); // 可以设置为 atom_1.0
        
        // 设置 Feed 基本信息
        feed.setTitle("我的技术博客");
        feed.setLink("https://mytechblog.com");
        feed.setDescription("分享最新技术文章和教程");
        
        // 创建条目列表
        List<SyndEntry> entries = new ArrayList<>();
        
        // 创建条目1
        SyndEntry entry1 = new SyndEntryImpl();
        entry1.setTitle("Java 17新特性详解");
        entry1.setLink("https://mytechblog.com/java17");
        entry1.setPublishedDate(new Date());
        
        // 设置条目1的描述
        SyndContent description1 = new SyndContentImpl();
        description1.setType("text/html");
        description1.setValue("<p>本文详细介绍了Java 17的新特性和改进...</p>");
        entry1.setDescription(description1);
        
        // 添加条目到列表
        entries.add(entry1);
        
        // 创建条目2
        SyndEntry entry2 = new SyndEntryImpl();
        entry2.setTitle("Spring Boot 3.0入门教程");
        entry2.setLink("https://mytechblog.com/spring-boot-3");
        entry2.setPublishedDate(new Date());
        
        // 设置条目2的描述
        SyndContent description2 = new SyndContentImpl();
        description2.setType("text/html");
        description2.setValue("<p>从零开始学习Spring Boot 3.0的完整教程...</p>");
        entry2.setDescription(description2);
        
        // 添加条目到列表
        entries.add(entry2);
        
        // 将所有条目添加到Feed
        feed.setEntries(entries);
        
        return feed;
    }
    
    public void writeFeedToXml(SyndFeed feed, Writer writer) throws Exception {
        SyndFeedOutput output = new SyndFeedOutput();
        output.output(feed, writer);
    }
}
```

### 4.3 输出到文件

```java
public void saveFeedToFile(SyndFeed feed, String filePath) throws Exception {
    Writer writer = new FileWriter(filePath);
    SyndFeedOutput output = new SyndFeedOutput();
    output.output(feed, writer);
    writer.close();
}
```

## 5. 高级功能

### 5.1 处理模块和扩展

Rome 支持各种 RSS 和 Atom 模块，如 Dublin Core、Content 模块等：

```java
import com.rometools.rome.feed.module.DCModule;
import com.rometools.rome.feed.module.Module;

// 获取 Dublin Core 模块
DCModule dcModule = (DCModule) entry.getModule(DCModule.URI);
if (dcModule != null) {
    System.out.println("DC Creator: " + dcModule.getCreator());
    System.out.println("DC Date: " + dcModule.getDate());
    System.out.println("DC Subject: " + dcModule.getSubject());
}
```

### 5.2 自定义 Feed 处理器

对于需要特殊处理的 Feed，可以实现自定义处理：

```java
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public SyndFeed parseWithCustomHandler(String feedUrl) throws Exception {
    URL url = new URL(feedUrl);
    SyndFeedInput input = new SyndFeedInput();
    
    // 配置自定义处理
    input.setPreserveWireFeed(true); // 保留原始 Wire Feed
    
    SyndFeed feed = input.build(new XmlReader(url));
    return feed;
}
```

### 5.3 错误处理

处理解析过程中可能出现的异常：

```java
public SyndFeed safeParseFeed(String feedUrl) {
    try {
        URL url = new URL(feedUrl);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(url));
        return feed;
    } catch (MalformedURLException e) {
        System.err.println("URL格式错误: " + e.getMessage());
    } catch (IllegalArgumentException e) {
        System.err.println("Feed参数错误: " + e.getMessage());
    } catch (FeedException e) {
        System.err.println("Feed解析错误: " + e.getMessage());
    } catch (IOException e) {
        System.err.println("IO错误: " + e.getMessage());
    }
    return null;
}
```

## 6. 最佳实践

### 6.1 性能优化

- **连接池**: 处理多个 Feed 时使用 HTTP 连接池
- **缓存**: 缓存已解析的 Feed 内容
- **异步处理**: 使用 CompletableFuture 或线程池并行处理多个 Feed

```java
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public List<SyndFeed> parseMultipleFeeds(List<String> feedUrls) {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    
    List<CompletableFuture<SyndFeed>> futures = feedUrls.stream()
        .map(url -> CompletableFuture.supplyAsync(() -> {
            try {
                return parseFeed(url);
            } catch (Exception e) {
                System.err.println("Error parsing feed " + url + ": " + e.getMessage());
                return null;
            }
        }, executor))
        .collect(Collectors.toList());
    
    List<SyndFeed> feeds = futures.stream()
        .map(CompletableFuture::join)
        .filter(feed -> feed != null)
        .collect(Collectors.toList());
    
    executor.shutdown();
    return feeds;
}
```

### 6.2 处理不同编码

Rome 的 `XmlReader` 会自动检测 XML 编码，但有时需要手动指定：

```java
public SyndFeed parseWithEncoding(String feedUrl, String encoding) throws Exception {
    URL url = new URL(feedUrl);
    SyndFeedInput input = new SyndFeedInput();
    return input.build(new XmlReader(url, encoding));
}
```

### 6.3 处理内容清理

一些 Feed 可能包含不安全的 HTML，可以使用 Jsoup 进行清理：

```java
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public String cleanHtml(String html) {
    return Jsoup.clean(html, Safelist.basic());
}
```

## 7. 常见问题解决

### 7.1 解析错误

**问题**: `FeedException: Invalid XML: DOCTYPE has been disallowed`

**解决方案**: 使用自定义 `XmlReader`：

```java
public SyndFeed parseWithoutDoctypeCheck(String feedUrl) throws Exception {
    URL url = new URL(feedUrl);
    XMLReaderFactory factory = new XMLReaderFactory() {
        @Override
        public XMLReader createXMLReader() throws SAXException {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            return reader;
        }
    };
    XmlReader xmlReader = new XmlReader(url);
    SyndFeedInput input = new SyndFeedInput();
    input.setXMLReaderFactory(factory);
    return input.build(xmlReader);
}
```

### 7.2 特殊字符问题

**问题**: Feed 中包含特殊字符导致解析失败

**解决方案**: 使用标准的 XML 解析器，并在读取前预处理内容：

```java
public SyndFeed parseWithCharacterHandling(String feedUrl) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(feedUrl))
        .build();
    
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    String content = response.body()
        .replaceAll("&(?!(amp;|lt;|gt;|quot;|apos;))", "&amp;"); // 处理未转义的 &
    
    SyndFeedInput input = new SyndFeedInput();
    return input.build(new StringReader(content));
}
```

### 7.3 重定向问题

**问题**: Feed URL 被重定向但无法正确跟踪

**解决方案**: 使用 HttpClient 处理重定向：

```java
public SyndFeed parseWithRedirects(String feedUrl) throws Exception {
    HttpClient client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(feedUrl))
        .build();
    
    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
    SyndFeedInput input = new SyndFeedInput();
    return input.build(new XmlReader(response.body()));
}
```

## 8. 实际应用示例

### 8.1 RssArticleService 实现

```java
import com.cq.RssHub.pojo.Article;
import com.cq.RssHub.pojo.RssSource;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RssArticleService {
    
    /**
     * 从RSS源获取文章
     * @param rssSource RSS源信息
     * @return 文章列表
     */
    public List<Article> fetchArticlesFromRss(RssSource rssSource) {
        List<Article> articles = new ArrayList<>();
        
        try {
            // 创建HTTP客户端
            HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
            
            // 创建请求
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(rssSource.getUrl()))
                .header("User-Agent", "RssHub/1.0")
                .GET()
                .build();
            
            // 发送请求并获取响应
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            
            // 解析Feed
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(response.body()));
            
            // 处理所有文章条目
            for (SyndEntry entry : feed.getEntries()) {
                Article article = convertEntryToArticle(entry, rssSource);
                articles.add(article);
            }
            
            // 更新RSS源信息
            rssSource.setLastFetchTime(LocalDateTime.now());
            rssSource.setArticleCount(rssSource.getArticleCount() + articles.size());
            
        } catch (Exception e) {
            // 记录错误信息
            rssSource.setErrorMessage(e.getMessage());
            e.printStackTrace();
        }
        
        return articles;
    }
    
    /**
     * 将SyndEntry转换为Article
     */
    private Article convertEntryToArticle(SyndEntry entry, RssSource rssSource) {
        Article article = new Article();
        
        // 设置基本信息
        article.setTitle(entry.getTitle());
        article.setLink(entry.getLink());
        article.setAuthor(entry.getAuthor());
        article.setSourceId(rssSource.getId());
        
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
            article.setSummary(entry.getDescription().getValue());
        }
        
        // 设置内容
        if (!entry.getContents().isEmpty()) {
            SyndContent content = entry.getContents().get(0);
            article.setContent(content.getValue());
        } else if (entry.getDescription() != null) {
            // 如果没有内容，使用摘要作为内容
            article.setContent(entry.getDescription().getValue());
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
     * 将Date转换为LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }
}
```

## 9. 参考资源

- [Rome 官方文档](https://rometools.github.io/rome/)
- [Rome GitHub 仓库](https://github.com/rometools/rome)
- [RSS 规范](https://www.rssboard.org/rss-specification)
- [Atom 规范](https://datatracker.ietf.org/doc/html/rfc4287)
