package com.cq.RssHub.service.impl;

import com.cq.RssHub.mapper.CategoryMapper;
import com.cq.RssHub.pojo.Category;
import com.cq.RssHub.pojo.vo.CategoryVO;
import com.cq.RssHub.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> getAllCategories() {
        List<Category> categories = categoryMapper.findAll();
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setDescription(category.getDescription());
        vo.setSourceCount(category.getSourceCount() != null ? category.getSourceCount() : 0);
        vo.setArticleCount(category.getArticleCount() != null ? category.getArticleCount() : 0);
        vo.setCreateTime(category.getCreateTime());
        return vo;
    }

    @Override
    public Category getCategoryById(Integer id) {
        return categoryMapper.findById(id);
    }

    @Override
    public int createCategory(Category category) {
        return categoryMapper.insert(category);
    }

    @Override
    public int updateCategory(Category category) {
        return categoryMapper.update(category);
    }

    @Override
    public int deleteCategory(Integer id) {
        return categoryMapper.deleteById(id);
    }
}
