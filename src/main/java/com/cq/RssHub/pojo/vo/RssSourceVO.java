package com.cq.RssHub.pojo.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RssSourceVO {
    private Integer id;
    private String name;
    private String url;
    private Integer categoryId;
    private String description;
    private Integer frequency;
    private LocalDateTime lastFetchTime;
    private String status;
    private Integer articleCount;
    
    // 非数据库字段
    private String categoryName;
}
