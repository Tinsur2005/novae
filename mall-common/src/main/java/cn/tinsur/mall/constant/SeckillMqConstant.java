package cn.tinsur.mall.constant;

/**
 * 秒杀 MQ 拓扑常量（生产端 seckill-service 与消费端 order-service 共用）
 */
public interface SeckillMqConstant {

    /** topic 交换机（普通交换机，非延时） */
    String SECKILL_TOPIC_EXCHANGE = "seckill.topic.exchange";

    /** 秒杀下单队列（order-service 消费落库） */
    String SECKILL_ORDER_QUEUE = "seckill.order.queue";

    /** 下单消息路由键 */
    String SECKILL_ORDER_ROUTING_KEY = "seckill.order";
}
