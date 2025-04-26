package com.cq.RssHub.mapper;

import com.cq.RssHub.pojo.FetchTask;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FetchTaskMapper {
    /**
     * 插入抓取任务记录
     */
    int insert(FetchTask fetchTask);
    
    /**
     * 更新抓取任务状态
     */
    int update(FetchTask fetchTask);
    
    /**
     * 查询最近的抓取任务
     */
    List<FetchTask> findRecentTasks(int limit);
    
    /**
     * 统计今日成功任务数
     */
    int countTodaySuccessfulTasks();
    
    /**
     * 统计今日失败任务数
     */
    int countTodayFailedTasks();
} 