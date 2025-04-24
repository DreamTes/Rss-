package com.cq.RssHub.mapper;

import com.cq.RssHub.pojo.FetchTask;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FetchTaskMapper {
    @Insert("INSERT INTO fetch_task (source_id, status, start_time, end_time, articles_added, error_message, create_time) " +
            "VALUES (#{sourceId}, #{status}, #{startTime}, #{endTime}, #{articlesAdded}, #{errorMessage}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FetchTask fetchTask);
    
    @Update("UPDATE fetch_task SET status = #{status}, end_time = #{endTime}, articles_added = #{articlesAdded}, " +
            "error_message = #{errorMessage} WHERE id = #{id}")
    int update(FetchTask fetchTask);
    
    @Select("SELECT ft.*, rs.name AS source_name FROM fetch_task ft " +
            "JOIN rss_source rs ON ft.source_id = rs.id " +
            "ORDER BY ft.start_time DESC LIMIT #{limit}")
    List<FetchTask> findRecentTasks(int limit);
    
    @Select("SELECT COUNT(*) FROM fetch_task WHERE status = 'completed' AND DATE(start_time) = CURDATE()")
    int countTodaySuccessfulTasks();
    
    @Select("SELECT COUNT(*) FROM fetch_task WHERE status = 'failed' AND DATE(start_time) = CURDATE()")
    int countTodayFailedTasks();
} 