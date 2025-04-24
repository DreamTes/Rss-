package com.cq.RssHub.pojo.vo;

import lombok.Data;

@Data
public class DashboardStatisticsVO {
    private Integer totalSources;
    private Integer totalArticles;
    private Integer todayNewArticles;
    private Integer categoryCount;
    private FetchStatusVO fetchStatus;
    
    @Data
    public static class FetchStatusVO {
        private Integer success;
        private Integer failed;
    }
} 