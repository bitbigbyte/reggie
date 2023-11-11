package com.xiaoke.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoke.dto.DishDto;
import com.xiaoke.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时操作两张表：dish,dish_flavor
    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);
}
