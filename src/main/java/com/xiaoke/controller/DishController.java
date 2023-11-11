package com.xiaoke.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoke.common.R;
import com.xiaoke.dto.DishDto;
import com.xiaoke.entity.*;
import com.xiaoke.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    @GetMapping("/page")
    public R<Page<DishDto>> page(int page,int pageSize,String name){
        Page<DishDto> dishDtoPage=new Page<>();

        Page<Dish> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<DishDto> list=new ArrayList<>();
        for(Dish dish:pageInfo.getRecords()){
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            Category category=categoryService.getById(dish.getCategoryId());
            // 设置分类名称
            if(category!=null) {
                dishDto.setCategoryName(category.getName());
            }
            list.add(dishDto);
        }
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }


    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }


    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("更新菜品成功");
    }


    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list=dishService.list(queryWrapper);
        List<DishDto> dishDtoList=new ArrayList<>();
        for(Dish item:list){
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            dishDto.setCategoryName(categoryService.getById(item.getCategoryId()).getName());
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,item.getId());
            List<DishFlavor> dishFlavors=dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            dishDtoList.add(dishDto);
        }
        return R.success(dishDtoList);
    }


    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids).eq(Dish::getStatus,1);
        long count=dishService.count(queryWrapper);
        if(count>0) {
            return R.error("套餐正在出售，不能删除");
        }
        dishService.removeByIds(ids);
        dishFlavorService.remove(new LambdaQueryWrapper<DishFlavor>().in(DishFlavor::getDishId,ids));
        setmealDishService.remove(new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getDishId,ids));
        return R.success("删除成功");
    }
}
