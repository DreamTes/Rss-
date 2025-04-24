package com.cq.RssHub.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FetchTask {
    private Integer id;
    private Integer sourceId;
    private String status; // completed, failed, running
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer articlesAdded;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 非数据库字段
    private String sourceName;
}
