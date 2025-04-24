package com.cq.RssHub.service;

import com.cq.RssHub.pojo.vo.DashboardStatisticsVO;
import com.cq.RssHub.pojo.vo.FetchTaskVO;
import java.util.List;

public interface DashboardService {
    /**
     * 获取仪表盘统计数据
     * @return 仪表盘统计数据
     */
    DashboardStatisticsVO getStatistics();
    
    /**
     * 获取最近的抓取任务
     * @param limit 获取数量限制
     * @return 抓取任务列表
     */
    List<FetchTaskVO> getRecentFetchTasks(Integer limit);
} 