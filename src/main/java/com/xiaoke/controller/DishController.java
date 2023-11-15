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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

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

        //清理所有菜品缓存
        //Set<String> keys=redisTemplate.keys("dish*");
        //if (keys!=null&&!keys.isEmpty()) {
        //   redisTemplate.delete(keys);
        //}

        //清理某个菜品缓存
        String key="dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();
        redisTemplate.delete(key);

        return R.success("更新菜品成功");
    }


    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        List<DishDto> dishDtoList=null;

        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();

        //从redis中获取缓存数据
        dishDtoList = (List<DishDto>)redisTemplate.opsForValue().get(key);

        //如果存在，直接返回，无需查询数据库
        if(dishDtoList!=null){
            return R.success(dishDtoList);
        }


        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list=dishService.list(queryWrapper);

       dishDtoList=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            dishDto.setCategoryName(categoryService.getById(item.getCategoryId()).getName());
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,item.getId());
            List<DishFlavor> dishFlavors=dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到redis中
        redisTemplate.opsForValue().set(key,dishDtoList,30, TimeUnit.MINUTES);

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
