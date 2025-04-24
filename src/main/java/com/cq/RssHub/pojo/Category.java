package com.cq.RssHub.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 统计字段
    private Integer sourceCount;
    private Integer articleCount;
}