package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.pojo.entity.OrderItem;
import cn.tinsur.mall.mapper.OrderItemMapper;
import cn.tinsur.mall.service.IOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细表 服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-18
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

}
