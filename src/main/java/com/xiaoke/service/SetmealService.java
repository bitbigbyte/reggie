package com.xiaoke.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoke.dto.SetmealDto;
import com.xiaoke.entity.Setmeal;
import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);
    void removeWithDish(List<Long> ids);
}
