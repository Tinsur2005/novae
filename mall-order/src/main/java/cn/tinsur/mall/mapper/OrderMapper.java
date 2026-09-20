package cn.tinsur.mall.mapper;

import cn.tinsur.mall.pojo.entity.Order;
import cn.tinsur.mall.pojo.vo.OrderVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-18
 */
public interface OrderMapper extends BaseMapper<Order> {

    List<OrderVO> listAll(Long userId);
}