package cn.tinsur.mall.pojo.vo;

import cn.tinsur.mall.api.pojo.entity.Shipping;
import cn.tinsur.mall.pojo.entity.Order;
import cn.tinsur.mall.pojo.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderVO extends Order {

    private List<OrderItem> orderItemList;

    //收货地址（秒杀订单支付前可能还未选择，为null）
    private Shipping shipping;
}