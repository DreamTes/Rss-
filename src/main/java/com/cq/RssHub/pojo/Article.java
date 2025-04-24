package com.cq.RssHub.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Article {
    private Integer id;
    private String title;
    private String link;
    private Integer sourceId;
    private String summary;
    private String content;
    private String author;
    private LocalDateTime publishDate;
    private Boolean isRead = false;
    private Boolean isStarred = false;
    private Integer readCount = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段，用于显示来源名称和分类
    private String sourceName;
    private String categoryName;
}
