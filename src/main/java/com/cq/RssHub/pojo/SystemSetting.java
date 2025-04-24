package com.cq.RssHub.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemSetting {
    private Integer id;
    private String settingKey;
    private String settingValue;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
