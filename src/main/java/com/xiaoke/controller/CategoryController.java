package com.xiaoke.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoke.common.R;
import com.xiaoke.entity.Category;
import com.xiaoke.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * TODO:分类管理
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * TODO:新增分类
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增分类:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * TODO:分类信息分页查询
     */
    @GetMapping("/page")
    public R<Page<Category>> page(@RequestParam int page,@RequestParam int pageSize){
        Page<Category> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * TODO:根据id删除分类
     */
    @DeleteMapping
    public R<String> delete(@RequestParam Long id){
        log.info("删除分类,id为{}",id);
        categoryService.removeById(id);
        return R.success("删除分类成功");
    }

    /**
     * TODO:根据id修改分类
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类,id为{}",category.getId());
        categoryService.updateById(category);
        return R.success("修改分类成功");
    }


    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        queryWrapper.orderByDesc(Category::getType);
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
