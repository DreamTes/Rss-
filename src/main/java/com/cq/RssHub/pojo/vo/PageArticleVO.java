package com.cq.RssHub.pojo.vo;

import com.cq.RssHub.pojo.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageArticleVO {
    private long total;
    private List<Article> items;
}
