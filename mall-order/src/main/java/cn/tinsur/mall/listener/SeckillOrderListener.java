package cn.tinsur.mall.listener;

import cn.tinsur.mall.api.product.ProductClient;
import cn.tinsur.mall.api.seckill.SeckillClient;
import cn.tinsur.mall.constant.SeckillMqConstant;
import cn.tinsur.mall.pojo.message.SeckillOrderMessage;
import cn.tinsur.mall.service.IOrderService;
import cn.tinsur.mall.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 秒杀下单消息消费者（订单数据归属方消费：orders 表在 mall 库，本地事务闭环）
 * 拓扑声明责任在消费端：seckill.topic.exchange / seckill.order.queue 随 order-service 启动才创建，
 * 首次联调必须先启动 order-service，否则生产端发送到不存在的交换机消息会丢失
 */
@Slf4j
@Component
public class SeckillOrderListener {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private SeckillClient seckillClient;
    @Autowired
    private ProductClient productClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(SeckillMqConstant.SECKILL_ORDER_QUEUE),
            exchange = @Exchange(name = SeckillMqConstant.SECKILL_TOPIC_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = SeckillMqConstant.SECKILL_ORDER_ROUTING_KEY
    ))
    public void listenSeckillOrder(SeckillOrderMessage message) {
        //1.订单落库（本地事务+订单号幂等）；落库抛异常时消息重回队列重试
        boolean created = orderService.createSeckillOrder(message);
        if (!created) {
            //重复消费，直接确认
            return;
        }

        //2.Feign 回调同步数据库库存（防超卖闸门是Redis预扣，这两步失败仅告警不回滚，最终一致）：
        //a.扣活动配额库存 activity_product.stock
        deductStockQuietly("活动配额", message, () ->
                seckillClient.deductStock(message.getActivityId(), message.getProductId(), message.getQuantity()));
        //b.扣商品真实库存 product.stock
        deductStockQuietly("商品真实", message, () ->
                productClient.deductStock(message.getProductId(), message.getQuantity()));
    }

    /**
     * 库存回调：失败仅告警不回滚（Redis 已扣、订单已落库，DB 库存靠最终一致追平）
     */
    private void deductStockQuietly(String stockType, SeckillOrderMessage message, Supplier<Result> deduct) {
        try {
            Result result = deduct.get();
            if (result == null || result.getCode() != Result.OK) {
                log.warn("秒杀订单{}回调扣减{}库存失败：{}", message.getOrderNo(), stockType,
                        result == null ? "无响应" : result.getMsg());
            }
        } catch (Exception e) {
            log.warn("秒杀订单{}回调扣减{}库存异常：{}", message.getOrderNo(), stockType, e.getMessage());
        }
    }
}
