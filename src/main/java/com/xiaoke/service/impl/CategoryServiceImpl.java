package com.xiaoke.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoke.common.CustomException;
import com.xiaoke.entity.Category;
import com.xiaoke.entity.Dish;
import com.xiaoke.entity.Setmeal;
import com.xiaoke.mapper.CategoryMapper;
import com.xiaoke.service.CategoryService;
import com.xiaoke.service.DishService;
import com.xiaoke.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * TODO:根据id删除分类，删除之前先判断
     */
    @Override
    public void removeById(Long id) {
        //查询当前分类是否关联了菜品，如果关联，则抛出异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        long count1 = dishService.count(dishLambdaQueryWrapper);
        if(count1>0L){
            throw new CustomException("当前分类下有菜品，不能删除");
        }
        //查询当前分类是否关联了套餐，如果关联，则抛出异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        long count2 = setmealService.count(setmealLambdaQueryWrapper);
        if(count2>0L) {
            throw new CustomException("当前分类下有套餐，不能删除");
        }
        //正常删除
        super.removeById(id);
    }
}
