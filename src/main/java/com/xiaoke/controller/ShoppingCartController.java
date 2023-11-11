package com.xiaoke.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoke.common.BaseContext;
import com.xiaoke.common.CustomException;
import com.xiaoke.common.R;
import com.xiaoke.entity.ShoppingCart;
import com.xiaoke.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("/shoppingCart")
@RestController
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据：{}", shoppingCart);
        //设置用户id,指定当前是哪个用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //查询当前菜品或者套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());
        if(shoppingCart.getDishId()!=null){
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else{
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartDB = shoppingCartService.getOne(queryWrapper);

        if(shoppingCartDB!=null){
            //如果已经存在，就在原来数量基础上加1
            shoppingCart=shoppingCartDB;
            shoppingCart.setNumber(shoppingCart.getNumber()+1);
            shoppingCartService.updateById(shoppingCart);
        }else{
            //如果不存在，则添加到购物车中，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }

        return R.success(shoppingCart);
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        shoppingCart.setUserId(BaseContext.getCurrentId());
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());
        if(shoppingCart.getDishId()!=null){
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart shoppingCartDB = shoppingCartService.getOne(queryWrapper);
        long count=shoppingCartService.count(queryWrapper);
        if(count==0){
            throw new CustomException("该菜品还未被加入购物车");
        }

        if(shoppingCartDB.getNumber()==1){
            shoppingCartService.removeById(shoppingCartDB);
        } else {
            shoppingCartDB.setNumber(shoppingCartDB.getNumber()-1);
            shoppingCartService.updateById(shoppingCartDB);
        }
        return R.success(shoppingCartDB);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        return R.success(shoppingCartList);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        shoppingCartService.remove(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId,BaseContext.getCurrentId()));
        return R.success("清空购物车成功");
    }
}
