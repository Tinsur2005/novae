package cn.tinsur.mall.service;

import cn.tinsur.mall.pojo.entity.Order;
import cn.tinsur.mall.pojo.query.OrderQuery;
import cn.tinsur.mall.pojo.vo.OrderVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-18
 */
public interface IOrderService extends IService<Order> {

    void add(Order order);

    List<OrderVO> listAll();

    IPage<OrderVO> page(OrderQuery orderQuery);

    void send(Long orderNo);

    void close(Long orderNo);

    void deleteById(Long orderNo);

    void cancelOrder(Long orderNo);

    void pay(Long orderNo);
}