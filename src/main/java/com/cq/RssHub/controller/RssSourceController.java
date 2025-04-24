package com.cq.RssHub.controller;

import com.cq.RssHub.pojo.ResponseMessage;
import com.cq.RssHub.pojo.RssSource;
import com.cq.RssHub.pojo.vo.PageRssSourceVO;
import com.cq.RssHub.pojo.vo.RssSourceVO;
import com.cq.RssHub.service.RssSourceService;
import com.cq.RssHub.service.impl.RssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/rss/sources")
public class RssSourceController {
    @Autowired
    private RssSourceService rssSourceService;
    
    @Autowired
    private RssService rssService;

    /**
     * 获取RSS源列表
     */
    @GetMapping
    public ResponseMessage<?> getRssSources(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status) {
        
        PageRssSourceVO sources = rssSourceService.getRssSources(page, pageSize, keyword, categoryId, status);
        return ResponseMessage.success("获取成功", sources);
    }

    /**
     * 添加新的RSS源
     */
    @PostMapping
    public ResponseMessage<?> createRssSource(@RequestBody RssSource rssSource) {
        int result = rssSourceService.createRssSource(rssSource);
        if (result > 0) {
            // 返回简化版本，只包含必要字段
            RssSourceVO vo = new RssSourceVO();
            vo.setId(rssSource.getId());
            vo.setName(rssSource.getName());
            vo.setUrl(rssSource.getUrl());
            vo.setCategoryId(rssSource.getCategoryId());
            vo.setDescription(rssSource.getDescription());
            vo.setFrequency(rssSource.getFrequency());
            vo.setStatus(rssSource.getStatus());
            
            return ResponseMessage.success("添加成功", vo);
        }
        return ResponseMessage.error("添加失败");
    }

    /**
     * 更新现有RSS源
     */
    @PutMapping("/{id}")
    public ResponseMessage<?> updateRssSource(@PathVariable Integer id, @RequestBody RssSource rssSource) {
        rssSource.setId(id);
        int result = rssSourceService.updateRssSource(rssSource);
        if (result > 0) {
            return ResponseMessage.success("更新成功");
        }
        return ResponseMessage.error("更新失败");
    }

    /**
     * 删除RSS源
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<?> deleteRssSource(@PathVariable Integer id) {
        int result = rssSourceService.deleteRssSource(id);
        if (result > 0) {
            return ResponseMessage.success("删除成功");
        }
        return ResponseMessage.error("删除失败");
    }

    /**
     * 立即抓取RSS源
     */
    @PostMapping("/{id}/fetch")
    public ResponseMessage<?> fetchRssSource(@PathVariable Integer id) {
        try {
            int newArticlesCount = rssService.fetchRssSourceNow(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", id.toString());
            result.put("newArticlesCount", newArticlesCount);
            
            return ResponseMessage.success("抓取任务已完成", result);
        } catch (Exception e) {
            return ResponseMessage.error("抓取失败: " + e.getMessage());
        }
    }
}
