package com.cq.RssHub.controller;

import com.cq.RssHub.pojo.ResponseMessage;
import com.cq.RssHub.pojo.vo.DashboardStatisticsVO;
import com.cq.RssHub.pojo.vo.FetchTaskVO;
import com.cq.RssHub.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public ResponseMessage<?> getStatistics() {
        DashboardStatisticsVO statistics = dashboardService.getStatistics();
        return ResponseMessage.success("获取成功", statistics);
    }

    /**
     * 获取最近抓取任务
     * @param limit 获取数量限制
     * @return 抓取任务列表
     */
    @GetMapping("/fetch-tasks")
    public ResponseMessage<?> getRecentFetchTasks(
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        List<FetchTaskVO> tasks = dashboardService.getRecentFetchTasks(limit);
        return ResponseMessage.success("获取成功", tasks);
    }
} 