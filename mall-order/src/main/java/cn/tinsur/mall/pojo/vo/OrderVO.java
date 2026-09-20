package cn.tinsur.mall.pojo.vo;

import cn.tinsur.mall.pojo.entity.Order;
import cn.tinsur.mall.pojo.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderVO extends Order {

    private List<OrderItem> orderItemList;
}