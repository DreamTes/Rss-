package com.cq.RssHub.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FetchTaskVO {
    private Integer id;
    private String sourceName;
    private Integer sourceId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer articlesAdded;
} 