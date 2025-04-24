package com.cq.RssHub.service;

import com.cq.RssHub.pojo.RssSource;
import com.cq.RssHub.pojo.vo.PageRssSourceVO;
import com.cq.RssHub.pojo.vo.RssSourceVO;

import java.util.List;

public interface RssSourceService {
    /**
     * 获取RSS源列表
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param status 状态
     * @return RSS源列表
     */
    PageRssSourceVO getRssSources(Integer page, Integer pageSize, String keyword, Integer categoryId, String status);

    /**
     * 根据ID获取RSS源
     * @param id RSS源ID
     * @return RSS源
     */
    RssSource getRssSourceById(Integer id);

    /**
     * 创建RSS源
     * @param rssSource RSS源
     * @return 影响的行数
     */
    int createRssSource(RssSource rssSource);

    /**
     * 更新RSS源
     * @param rssSource RSS源
     * @return 影响的行数
     */
    int updateRssSource(RssSource rssSource);

    /**
     * 删除RSS源
     * @param id RSS源ID
     * @return 影响的行数
     */
    int deleteRssSource(Integer id);
    
    /**
     * 更新RSS源的最后抓取时间
     * @param id RSS源ID
     * @return 影响的行数
     */
    int updateLastFetchTime(Integer id);
}
