package com.cq.RssHub.pojo.vo;

import com.cq.RssHub.pojo.RssSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRssSourceVO {
    private long total;
    private List<RssSource> items;
}
