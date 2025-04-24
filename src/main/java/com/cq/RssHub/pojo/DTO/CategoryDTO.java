package com.cq.RssHub.pojo.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer sourceCount;
    private Integer articleCount;
    private LocalDateTime createTime;
} 