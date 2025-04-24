package com.cq.RssHub.mapper;

import com.cq.RssHub.pojo.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 查询所有分类
     */
    List<Category> findAll();
    
    /**
     * 查询所有分类，包含统计信息
     */
    List<Category> findAllWithStats();
    
    /**
     * 根据ID查询分类
     */
    Category findById(Integer id);
    
    /**
     * 插入分类
     */
    int insert(Category category);
    
    /**
     * 更新分类
     */
    int update(Category category);
    
    /**
     * 删除分类
     */
    int deleteById(Integer id);
    
    /**
     * 统计分类总数
     */
    @Select("SELECT COUNT(*) FROM category")
    int countTotal();
} 