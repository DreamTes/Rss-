package com.cq.RssHub.service.impl;

import com.cq.RssHub.mapper.RssSourceMapper;
import com.cq.RssHub.pojo.RssSource;
import com.cq.RssHub.pojo.vo.PageRssSourceVO;
import com.cq.RssHub.pojo.vo.RssSourceVO;
import com.cq.RssHub.service.RssSourceService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class RssSourceServiceImpl implements RssSourceService {
    
    @Autowired
    private RssSourceMapper rssSourceMapper;

    @Override
    public PageRssSourceVO getRssSources(Integer page, Integer pageSize, String keyword, Integer categoryId, String status) {
        log.info("获取RSS源列表: keyword={}, categoryId={}, status={}", keyword, categoryId, status);
        
        // 处理分页参数
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        //创建pageVo对象
        PageRssSourceVO pageVo = new PageRssSourceVO();
        //pageHelper
        PageHelper.startPage(page, pageSize);
        //调用mapper
        List<RssSource> as = rssSourceMapper.findByFilters(keyword, categoryId, status);
        Page<RssSource> p = (Page<RssSource>) as;

        pageVo.setTotal(p.getTotal());
        pageVo.setItems(p.getResult());

        return pageVo;
    }

    @Override
    public RssSource getRssSourceById(Integer id) {
        log.info("获取RSS源: id={}", id);
        return rssSourceMapper.findById(id);
    }

    @Override
    public int createRssSource(RssSource rssSource) {
        log.info("创建RSS源: {}", rssSource.getName());
        
        // 设置默认值
        if (rssSource.getFrequency() == null) {
            rssSource.setFrequency(60); // 默认60分钟
        }
        if (rssSource.getStatus() == null) {
            rssSource.setStatus("active");
        }
        if (rssSource.getArticleCount() == null) {
            rssSource.setArticleCount(0);
        }
        
        // 创建时间和更新时间由数据库生成
        
        return rssSourceMapper.insert(rssSource);
    }

    @Override
    public int updateRssSource(RssSource rssSource) {
        log.info("更新RSS源: id={}, name={}", rssSource.getId(), rssSource.getName());
        
        // 获取原数据
        RssSource existingSource = rssSourceMapper.findById(rssSource.getId());
        if (existingSource == null) {
            log.error("更新RSS源失败: 找不到ID为{}的RSS源", rssSource.getId());
            return 0;
        }
        
        // 保留原始不应修改的字段
        rssSource.setLastFetchTime(existingSource.getLastFetchTime());
        rssSource.setArticleCount(existingSource.getArticleCount());
        
        return rssSourceMapper.update(rssSource);
    }

    @Override
    public int deleteRssSource(Integer id) {
        log.info("删除RSS源: id={}", id);
        return rssSourceMapper.deleteById(id);
    }

    @Override
    public int updateLastFetchTime(Integer id) {
        log.info("更新RSS源最后抓取时间: id={}", id);
        return rssSourceMapper.updateLastFetchTime(id);
    }
}
