package com.xiaoke.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoke.common.BaseContext;
import com.xiaoke.common.CustomException;
import com.xiaoke.common.R;
import com.xiaoke.entity.*;
import com.xiaoke.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author xiaoke
 * @since 2023-11-11
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orders.setUserId(BaseContext.getCurrentId());
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,orders.getUserId());
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if(shoppingCarts==null||shoppingCarts.isEmpty()){
            throw new CustomException("购物车为空，不能下单");
        }
        User user = userService.getById(orders.getUserId());
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if(user==null||addressBook==null){
            throw new CustomException("用户或收货地址不存在");
        }

        BigDecimal total=new BigDecimal(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail=new OrderDetail();
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount().multiply(new BigDecimal(item.getNumber())));
            orderDetail.setImage(item.getImage());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            return orderDetail;
        }).collect(Collectors.toList());

        for(OrderDetail orderDetail:orderDetails){
            total=total.add(orderDetail.getAmount());
        }

        orders.setUserName(user.getName());
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setConsignee(addressBook.getConsignee());
        orders.setStatus(2);
        orders.setAddress((addressBook.getProvinceName()==null?"":addressBook.getProvinceName())
                +(addressBook.getCityName()==null?"":addressBook.getCityName())
                +(addressBook.getDistrictName()==null?"":addressBook.getDistrictName())
                +(addressBook.getDetail()==null?"":addressBook.getDetail()));
        orders.setAmount(total);
        orders.setPhone(user.getPhone());
        ordersService.save(orders);
        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.setOrderId(orders.getId());
        }
        orderDetailService.saveBatch(orderDetails);

        shoppingCartService.remove(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId,BaseContext.getCurrentId()));
        return R.success("下单成功");
    }
}

