package com.cq.RssHub.pojo.vo;

import com.cq.RssHub.pojo.ArticleFavorite;
import lombok.Data;

import java.util.List;

/**
 * 文章收藏分页视图对象
 */
@Data
public class PageArticleFavoriteVO {
    private List<ArticleFavorite> favorites;
    private int total;
    private int page;
    private int pageSize;
    private int totalPages;
} 