package com.xiaoke.service.impl;

import com.xiaoke.entity.Orders;
import com.xiaoke.mapper.OrdersMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoke.service.OrdersService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author xiaoke
 * @since 2023-11-11
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

}
