package com.cq.RssHub.controller;

import com.cq.RssHub.pojo.Category;
import com.cq.RssHub.pojo.ResponseMessage;
import com.cq.RssHub.pojo.vo.CategoryVO;
import com.cq.RssHub.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
//查看所有分类
    @GetMapping
    public ResponseMessage<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = categoryService.getAllCategories();
        return ResponseMessage.success("获取成功", categories);
    }
//添加分类
    @PostMapping
    public ResponseMessage<CategoryVO> createCategory(@RequestBody Category category) {
        int result = categoryService.createCategory(category);
        if (result > 0) {
            // 创建VO对象作为返回值
            CategoryVO categoryVO = new CategoryVO();
            categoryVO.setId(category.getId());
            categoryVO.setName(category.getName());
            categoryVO.setDescription(category.getDescription());
            
            return ResponseMessage.success("添加成功", categoryVO);
        }
        return ResponseMessage.error("创建失败");
    }
//更新分类
    @PutMapping("/{id}")
    public ResponseMessage<?> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        category.setId(id);
        int result = categoryService.updateCategory(category);
        if (result > 0) {
            return ResponseMessage.success("更新成功");
        }
        return ResponseMessage.error("更新失败");
    }
//删除分类
    @DeleteMapping("/{id}")
    public ResponseMessage<?> deleteCategory(@PathVariable Integer id) {
        int result = categoryService.deleteCategory(id);
        if (result > 0) {
            return ResponseMessage.success("删除成功");
        }
        return ResponseMessage.error("删除失败");
    }
}
