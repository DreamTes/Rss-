package com.cq.RssHub.service;

import com.cq.RssHub.pojo.Category;
import com.cq.RssHub.pojo.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    List<CategoryVO> getAllCategories();
    Category getCategoryById(Integer id);
    int createCategory(Category category);
    int updateCategory(Category category);
    int deleteCategory(Integer id);
} 