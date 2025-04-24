package com.cq.RssHub.pojo.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonInclude(JsonInclude.Include.NON_NULL) 注解是 Jackson 库提供的一个功能，主要用于控制 JSON 序列化过程中哪些字段应该被包含在最终的 JSON 输出中。
//基本解释
//这个注解应用在类或字段上，用来指示 Jackson 在将对象转换为 JSON 时，如何处理特定值（例如 null、空集合等）。
//具体功能
//@JsonInclude - 控制序列化过程中字段的包含策略
//JsonInclude.Include.NON_NULL - 特定的策略值，表示"不包括值为 null 的字段"
//参数选项
//JsonInclude.Include 枚举提供了多种策略选项：
//NON_NULL - 不包含值为 null 的字段
//NON_EMPTY - 不包含空集合、空数组、空字符串和 null 值的字段
//NON_DEFAULT - 不包含具有默认值的字段（如 int 为 0，boolean 为 false）
//NON_ABSENT - 不包含 null 和 Java 8 的 Optional.empty() 的字段
//ALWAYS - 始终包含字段，无论其值如何（默认行为）
//USE_DEFAULTS - 使用父类或默认的包含策略
public class CategoryVO {
    private Integer id;
    private String name;
    private String description;
    private Integer sourceCount;
    private Integer articleCount;
    private LocalDateTime createTime;
} 