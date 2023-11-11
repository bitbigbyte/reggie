package com.xiaoke.service.impl;

import com.xiaoke.entity.OrderDetail;
import com.xiaoke.mapper.OrderDetailMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoke.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细表 服务实现类
 * </p>
 *
 * @author xiaoke
 * @since 2023-11-11
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}
