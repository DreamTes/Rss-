package com.cq.RssHub.service;

import com.cq.RssHub.pojo.Article;
import com.cq.RssHub.pojo.RssSource;
import com.cq.RssHub.service.impl.RssParserService;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RssParserServiceTest {

    @Spy
    @InjectMocks
    private RssParserService rssParserService;

    private SyndFeed mockFeed;
    private SyndEntry mockEntry;
    private RssSource mockSource;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // 创建模拟Feed
        mockFeed = new SyndFeedImpl();
        mockFeed.setTitle("Test Feed");
        mockFeed.setLink("http://test.feed.com");
        mockFeed.setDescription("Test Feed Description");
        
        // 创建模拟Entry
        mockEntry = new SyndEntryImpl();
        mockEntry.setTitle("Test Article");
        mockEntry.setLink("http://test.feed.com/article");
        mockEntry.setPublishedDate(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));
        
        // 创建模拟内容
        SyndContent content = new SyndContentImpl();
        content.setValue("<p>这是一篇测试文章的内容。</p><img src='http://test.com/image.jpg'/><p>更多内容在这里。</p>");
        content.setType("text/html");
        
        List<SyndContent> contents = new ArrayList<>();
        contents.add(content);
        mockEntry.setContents(contents);
        
        // 创建模拟描述
        SyndContent description = new SyndContentImpl();
        description.setValue("文章摘要描述");
        description.setType("text/plain");
        mockEntry.setDescription(description);
        
        // 设置模拟作者
        mockEntry.setAuthor("测试作者");
        
        // 添加模拟Entry到Feed
        List<SyndEntry> entries = new ArrayList<>();
        entries.add(mockEntry);
        mockFeed.setEntries(entries);
        
        // 创建模拟RSS源
        mockSource = new RssSource();
        mockSource.setId(1);
        mockSource.setUrl("http://test.feed.com");
    }

    @Test
    public void testConvertToArticle_Success() {
        // 调用被测试方法
        Article result = rssParserService.convertToArticle(mockEntry, mockSource);
        
        // 验证基本信息转换
        assertNotNull(result);
        assertEquals("Test Article", result.getTitle());
        assertEquals("http://test.feed.com/article", result.getLink());
        assertEquals(1, result.getSourceId());
        assertEquals("测试作者", result.getAuthor());
        
        // 验证内容和摘要
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("这是一篇测试文章的内容"));
        assertNotNull(result.getSummary());
        
        // 验证发布日期
        assertNotNull(result.getPublishDate());
        
        // 验证封面图提取
        assertNotNull(result.getCoverImage());
        assertTrue(result.getCoverImage().contains("http://test.com/image.jpg") 
                || result.getCoverImage().contains("placeholder"));
    }

    @Test
    public void testExtractFirstImage_WithImage() {
        // 准备测试数据
        String htmlContent = "<p>测试内容</p><img src='http://test.com/image.jpg' alt='测试图片'/>";
        
        // 调用被测试方法
        String result = rssParserService.extractFirstImage(htmlContent);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("http://test.com/image.jpg", result);
    }

    @Test
    public void testExtractFirstImage_NoImage() {
        // 准备测试数据
        String htmlContent = "<p>测试内容没有图片</p>";
        
        // 调用被测试方法
        String result = rssParserService.extractFirstImage(htmlContent);
        
        // 验证结果
        assertNull(result);
    }



    @Test
    public void testParseFeedAsync_Success() throws Exception {
        // 模拟parseFeed方法的行为
        doReturn(mockFeed).when(rssParserService).parseFeed(anyString());
        
        // 调用被测试方法
        CompletableFuture<SyndFeed> futureResult = rssParserService.parseFeedAsync("http://test.feed.com");
        
        // 等待异步操作完成
        SyndFeed result = futureResult.get();
        
        // 验证结果
        assertNotNull(result);
        assertEquals("Test Feed", result.getTitle());
        assertEquals(1, result.getEntries().size());
        
        // 验证parseFeed方法被调用
        verify(rssParserService, times(1)).parseFeed("http://test.feed.com");
    }

    @Test
    public void testParseFeedAsync_Exception() throws Exception {
        // 模拟parseFeed方法抛出异常
        doThrow(new IOException("网络错误")).when(rssParserService).parseFeed(anyString());
        
        // 调用被测试方法
        CompletableFuture<SyndFeed> futureResult = rssParserService.parseFeedAsync("http://test.feed.com");
        
        // 验证异步结果包含异常
        Exception exception = assertThrows(ExecutionException.class, () -> futureResult.get());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertTrue(exception.getCause().getMessage().contains("解析失败"));
        
        // 验证parseFeed方法被调用
        verify(rssParserService, times(1)).parseFeed("http://test.feed.com");
    }

    @Test
    public void testIsWithinTimeRange_True() {
        // 准备测试数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime publishDate = now.minusDays(3);
        
        // 调用被测试方法
        boolean result = rssParserService.isWithinTimeRange(publishDate, 7);
        
        // 验证结果
        assertTrue(result);
    }

    @Test
    public void testIsWithinTimeRange_False() {
        // 准备测试数据
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime publishDate = now.minusDays(10);
        
        // 调用被测试方法
        boolean result = rssParserService.isWithinTimeRange(publishDate, 7);
        
        // 验证结果
        assertFalse(result);
    }
} 