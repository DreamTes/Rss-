package com.cq.RssHub.pojo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RssSource {
    private Integer id;
    private String name;
    private String url;
    private Integer categoryId;
    private String description;
    private Integer frequency = 60;
    private LocalDateTime lastFetchTime;
    private String status = "active";
    private String errorMessage;
    private Integer articleCount = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段，用于显示分类名称
    private String categoryName;
}
