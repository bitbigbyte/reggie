package com.xiaoke.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoke.common.R;
import com.xiaoke.dto.SetmealDto;
import com.xiaoke.entity.Category;
import com.xiaoke.entity.Setmeal;
import com.xiaoke.entity.SetmealDish;
import com.xiaoke.service.CategoryService;
import com.xiaoke.service.SetmealDishService;
import com.xiaoke.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name){
        Page<SetmealDto> dtoPage=new Page<>();

        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        List<SetmealDto> list=new ArrayList<>();
        for(Setmeal setmeal:pageInfo.getRecords()){
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);
            Category category=categoryService.getById(setmeal.getCategoryId());
            if(category!=null){
                setmealDto.setCategoryName(category.getName());
            }
            list.add(setmealDto);
        }
        dtoPage.setRecords(list);

        return R.success(dtoPage);
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        Setmeal setmeal=setmealService.getById(id);

        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list= setmealDishService.list(queryWrapper);

        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper=new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(Category::getId,setmeal.getCategoryId());
        Category category= categoryService.getOne(categoryLambdaQueryWrapper);

        SetmealDto setmealDto=new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setSetmealDishes(list);
        setmealDto.setCategoryName(category.getName());

        return R.success(setmealDto);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("要删除的套餐id是{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        log.info("要查询的套餐是{}", setmeal);
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
