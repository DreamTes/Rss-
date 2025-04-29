package com.cq.RssHub.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 文章收藏实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleFavorite {
    private Integer id;
    private Integer userId;
    private Integer articleId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 非数据库字段，用于展示
    private String articleTitle;
    private String coverImage;
    private String sourceName;
    private String categoryName;
} 