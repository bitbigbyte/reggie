package com.xiaoke.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoke.entity.Category;

public interface CategoryService extends IService<Category> {
    //接口中的成员变量默认是public,static,final类型的
    //接口中方法默认是public,abstract类型的
    void removeById(Long id);
}
