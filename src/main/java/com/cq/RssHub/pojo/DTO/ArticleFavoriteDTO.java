package com.cq.RssHub.pojo.DTO;

import lombok.Data;

/**
 * 文章收藏数据传输对象
 */
@Data
public class ArticleFavoriteDTO {
    private Integer articleId;
    private Integer userId;
} 