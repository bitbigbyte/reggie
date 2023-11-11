package com.xiaoke.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoke.dto.DishDto;
import com.xiaoke.entity.Dish;
import com.xiaoke.entity.DishFlavor;
import com.xiaoke.mapper.DishMapper;
import com.xiaoke.service.DishFlavorService;
import com.xiaoke.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);

        Long dishId=dishDto.getId();

        List<DishFlavor> flavors=dishDto.getFlavors();

        for(DishFlavor flavor:flavors){
            flavor.setDishId(dishId);
        }

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 1. 查询菜品
        Dish dish=this.getById(id);
        DishDto dishDto=new DishDto();

        // 2. 查询菜品口味
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavors=dishFlavorService.list(queryWrapper);

        // 3. 设置到菜品对象
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);
        Long dishId=dishDto.getId();

        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishId);
        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> flavors=dishDto.getFlavors();
        for(DishFlavor flavor:flavors){
            flavor.setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);
    }
}
