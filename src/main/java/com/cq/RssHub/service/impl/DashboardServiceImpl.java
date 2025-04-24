package com.cq.RssHub.service.impl;

import com.cq.RssHub.mapper.ArticleMapper;
import com.cq.RssHub.mapper.CategoryMapper;
import com.cq.RssHub.mapper.FetchTaskMapper;
import com.cq.RssHub.mapper.RssSourceMapper;
import com.cq.RssHub.pojo.FetchTask;
import com.cq.RssHub.pojo.vo.DashboardStatisticsVO;
import com.cq.RssHub.pojo.vo.FetchTaskVO;
import com.cq.RssHub.service.DashboardService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private RssSourceMapper rssSourceMapper;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private FetchTaskMapper fetchTaskMapper;

    @Override
    public DashboardStatisticsVO getStatistics() {
        DashboardStatisticsVO statistics = new DashboardStatisticsVO();
        
        // 获取RSS源总数
        statistics.setTotalSources(rssSourceMapper.countTotal());
        
        // 获取文章总数
        statistics.setTotalArticles(articleMapper.countTotal());
        
        // 获取今日新文章数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        statistics.setTodayNewArticles(articleMapper.countByDateRange(todayStart, todayEnd));
        
        // 获取分类总数
        statistics.setCategoryCount(categoryMapper.countTotal());
        
        // 获取抓取状态
        DashboardStatisticsVO.FetchStatusVO fetchStatus = new DashboardStatisticsVO.FetchStatusVO();
        fetchStatus.setSuccess(fetchTaskMapper.countTodaySuccessfulTasks());
        fetchStatus.setFailed(fetchTaskMapper.countTodayFailedTasks());
        statistics.setFetchStatus(fetchStatus);
        
        return statistics;
    }

    @Override
    public List<FetchTaskVO> getRecentFetchTasks(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 5; // 默认获取5条
        }
        
        List<FetchTask> tasks = fetchTaskMapper.findRecentTasks(limit);
        
        return tasks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    private FetchTaskVO convertToVO(FetchTask fetchTask) {
        FetchTaskVO vo = new FetchTaskVO();
        BeanUtils.copyProperties(fetchTask, vo);
        return vo;
    }
} 