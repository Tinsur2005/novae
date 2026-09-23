package cn.tinsur.mall.listener;

import cn.tinsur.mall.constant.MqConstant;
import cn.tinsur.mall.enums.OrderStatus;
import cn.tinsur.mall.pojo.entity.Order;
import cn.tinsur.mall.service.IOrderService;
import cn.tinsur.mall.util.MultiDelayMessage;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderStatusListener {
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(MqConstant.DELAY_ORDER_QUEUE),
            exchange = @Exchange(name = MqConstant.DELAY_EXCHANGE, delayed = "true"),
            key = MqConstant.DELAY_ORDER_ROUTING_KEY
    ))
    public void listenDelayMessage(MultiDelayMessage<Long> msg) {
        System.out.println("listenDelayMessage: ");
        //1.获取消息中的订单号
        Long orderNo = msg.getData();
        //2.查询订单，判断状态
        Order order = orderService.getById(orderNo);
        //订单不存在或者订单已经不是未付款状态，直接返回 -1-未付款 0-已取消 1-待发货 2-已发货 3-交易成功 4-交易关闭 5-已退款
        if (order == null || order.getStatus() != OrderStatus.UNPAID.getCode()) {
            return;
        }

        //3.订单未付款，判断是否还有剩余的延时时间
        if (msg.hasNextDelay()) {
            List<Long> delayMillis = msg.getDelayMillis();
            System.out.println("delayMillis: " + delayMillis);
            Long delay = msg.removeNextDelay();
            rabbitTemplate.convertAndSend(MqConstant.DELAY_EXCHANGE, MqConstant.DELAY_ORDER_ROUTING_KEY, msg, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setDelay(Math.toIntExact(delay));
                    return message;
                }
            });
        } else {
            //4.订单未付款，没有剩余的延时时间，取消订单
            orderService.cancelOrder(orderNo);
        }

    }
}