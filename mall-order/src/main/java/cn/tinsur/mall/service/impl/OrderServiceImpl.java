package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.api.cart.CartClient;
import cn.tinsur.mall.api.pojo.entity.Product;
import cn.tinsur.mall.api.pojo.vo.CartVO;
import cn.tinsur.mall.api.product.ProductClient;
import cn.tinsur.mall.constant.MqConstant;
import cn.tinsur.mall.enums.OrderStatus;
import cn.tinsur.mall.exception.ServiceException;
import cn.tinsur.mall.pojo.entity.Order;
import cn.tinsur.mall.mapper.OrderMapper;
import cn.tinsur.mall.pojo.entity.OrderItem;
import cn.tinsur.mall.mapper.OrderItemMapper;
import cn.tinsur.mall.pojo.query.OrderQuery;
import cn.tinsur.mall.pojo.vo.OrderVO;
import cn.tinsur.mall.service.IOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.tinsur.mall.util.LoginContext;
import cn.tinsur.mall.util.MultiDelayMessage;
import cn.tinsur.mall.util.Result;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-18
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartClient cartClient;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void add(Order order) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        order.setUserId(userId);
        //获取这个用户购物车里面已经勾选的商品
        Result<List<CartVO>> result = cartClient.list();
        if (result.getCode() == Result.ERROR) {
            throw new ServiceException("获取购物车数据失败");
        }
        List<CartVO> selectedCartVOList = result.getData().stream()
                .filter(cartVO -> cartVO.getSelected() == 1)
                .toList();
        //一件商品都没勾选时不让下单，否则会生成一个金额为0的空订单
        if (selectedCartVOList.isEmpty()) {
            throw new ServiceException("购物车中没有勾选的商品");
        }

        //校验库存并计算订单总金额，商品信息以商品服务的最新数据为准
        BigDecimal payment = BigDecimal.ZERO;
        for (CartVO cartVO : selectedCartVOList) {
            Result<Product> productResult = productClient.selectById(cartVO.getProductId());
            if (productResult.getCode() == Result.ERROR || productResult.getData() == null) {
                throw new ServiceException("商品不存在或已下架");
            }
            Product product = productResult.getData();
            if (product.getStock() < cartVO.getCount()) {
                throw new ServiceException(product.getName() + "库存不足");
            }
            //把最新的商品信息放回购物车项，插入订单明细时直接当商品快照用
            cartVO.setProduct(product);
            payment = payment.add(product.getPrice().multiply(BigDecimal.valueOf(cartVO.getCount())));
        }

        order.setStatus(OrderStatus.UNPAID.getCode());//-1:未付款
        order.setPaymentType(1);//1:微信支付
        order.setPostage(BigDecimal.ZERO);//运费0元
        order.setPayment(payment);
        //插入完订单之后，就可以获取雪花算法生成的订单号
        orderMapper.insert(order);

        //把当前用户购物车里面勾选的这些商品插入order_item
        for (CartVO cartVO : selectedCartVOList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderNo(order.getOrderNo());
            orderItem.setUserId(userId);
            orderItem.setProductId(cartVO.getProductId());
            orderItem.setProductName(cartVO.getProduct().getName());
            orderItem.setProductImage(cartVO.getProduct().getMainImage());
            orderItem.setCurrentUnitPrice(cartVO.getProduct().getPrice());
            orderItem.setQuantity(cartVO.getCount());

            BigDecimal productPrice = cartVO.getProduct().getPrice();
            BigDecimal quantity = BigDecimal.valueOf(cartVO.getCount());
            orderItem.setTotalPrice(productPrice.multiply(quantity));
            orderItemMapper.insert(orderItem);
        }

        //扣减库存
        deductStock(selectedCartVOList);

        //清除购物车已经下单的商品
        selectedCartVOList.forEach(cartVO -> cartClient.deletedById(cartVO.getId()));

        //发送延时消息：1分钟 + 5分钟 + 10分钟 + 14分钟，合计30分钟后取消订单
        MultiDelayMessage<Long> multiDelayMessage = new MultiDelayMessage<>(order.getOrderNo(), 60000L, 300000L, 600000L, 840000L);
        Long delay = multiDelayMessage.removeNextDelay();
        rabbitTemplate.convertAndSend(MqConstant.DELAY_EXCHANGE, MqConstant.DELAY_ORDER_ROUTING_KEY, multiDelayMessage, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDelay(Math.toIntExact(delay));
                return message;
            }
        });
    }

    /**
     * 扣减库存：商品服务是独立的事务，本地事务回滚不了它，
     * 所以中途失败时手动把已经扣掉的那部分库存补回去，避免库存凭空消失
     */
    private void deductStock(List<CartVO> cartVOList) {
        List<CartVO> deductedList = new ArrayList<>();
        try {
            for (CartVO cartVO : cartVOList) {
                Result deductResult = productClient.deductStock(cartVO.getProductId(), cartVO.getCount());
                if (deductResult.getCode() == Result.ERROR) {
                    throw new ServiceException(deductResult.getMsg());
                }
                deductedList.add(cartVO);
            }
        } catch (RuntimeException e) {
            deductedList.forEach(cartVO -> productClient.restoreStock(cartVO.getProductId(), cartVO.getCount()));
            throw e;
        }
    }

    @Override
    public List<OrderVO> listAll() {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        List<OrderVO> list = orderMapper.listAll(userId);
        return list;
    }

    @Override
    public IPage<OrderVO> page(OrderQuery orderQuery) {
        IPage<Order> page = new Page<>(orderQuery.getPage(), orderQuery.getLimit());
        LambdaQueryWrapper<Order> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(!ObjectUtils.isEmpty(orderQuery.getOrderNo()), Order::getOrderNo, orderQuery.getOrderNo())
                .eq(!ObjectUtils.isEmpty(orderQuery.getUserId()), Order::getUserId, orderQuery.getUserId())
                .eq(!ObjectUtils.isEmpty(orderQuery.getStatus()), Order::getStatus, orderQuery.getStatus())
                .between(!ObjectUtils.isEmpty(orderQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(orderQuery.getEndCreateTime()), Order::getCreateTime, orderQuery.getBeginCreateTime(), orderQuery.getEndCreateTime())
                .orderByDesc(Order::getCreateTime);
        orderMapper.selectPage(page, lambdaQueryWrapper);

        //每条订单再查一次它的商品明细
        List<OrderVO> orderVOList = page.getRecords().stream().map(order -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            LambdaQueryWrapper<OrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
            itemQueryWrapper.eq(OrderItem::getOrderNo, order.getOrderNo());
            orderVO.setOrderItemList(orderItemMapper.selectList(itemQueryWrapper));
            return orderVO;
        }).collect(Collectors.toList());

        IPage<OrderVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(orderVOList);
        return voPage;
    }

    //发货：支付模块还没做，未付款的订单也允许发货，发货后写发货时间
    @Override
    public void send(Long orderNo) {
        Order dbOrder = orderMapper.selectById(orderNo);
        if (dbOrder == null) {
            throw new ServiceException("订单不存在");
        }
        if (dbOrder.getStatus() != OrderStatus.UNPAID.getCode()
                && dbOrder.getStatus() != OrderStatus.WAIT_SEND.getCode()) {
            throw new ServiceException("当前订单状态不能发货");
        }
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setStatus(OrderStatus.SENT.getCode());
        order.setSendTime(new Date());
        orderMapper.updateById(order);
    }

    //关闭订单：下单时已经扣过库存了，关闭时要把库存加回去
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void close(Long orderNo) {
        Order dbOrder = orderMapper.selectById(orderNo);
        if (dbOrder == null) {
            throw new ServiceException("订单不存在");
        }
        if (dbOrder.getStatus() != OrderStatus.UNPAID.getCode()
                && dbOrder.getStatus() != OrderStatus.WAIT_SEND.getCode()
                && dbOrder.getStatus() != OrderStatus.SENT.getCode()) {
            throw new ServiceException("当前订单状态不能关闭");
        }
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setStatus(OrderStatus.CLOSED.getCode());
        order.setCloseTime(new Date());
        orderMapper.updateById(order);

        //按订单明细把商品库存加回去
        LambdaQueryWrapper<OrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(OrderItem::getOrderNo, orderNo);
        List<OrderItem> orderItemList = orderItemMapper.selectList(itemQueryWrapper);
        for (OrderItem orderItem : orderItemList) {
            Result restoreResult = productClient.restoreStock(orderItem.getProductId(), orderItem.getQuantity());
            if (restoreResult.getCode() == Result.ERROR) {
                throw new ServiceException(restoreResult.getMsg());
            }
        }
    }

    //支付：虚拟支付，暂时不接入第三方支付，支付后订单进入待发货状态
    @Override
    public void pay(Long orderNo) {
        Order dbOrder = orderMapper.selectById(orderNo);
        if (dbOrder == null) {
            throw new ServiceException("订单不存在");
        }
        //只能支付自己的订单
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        if (!dbOrder.getUserId().equals(userId)) {
            throw new ServiceException("只能支付自己的订单");
        }
        if (dbOrder.getStatus() != OrderStatus.UNPAID.getCode()) {
            throw new ServiceException("当前订单状态不能支付");
        }
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setStatus(OrderStatus.WAIT_SEND.getCode());
        order.setPaymentTime(new Date());
        orderMapper.updateById(order);
    }

    //取消订单：超时未付款自动取消，下单时扣的库存要加回去
    @Override
    public void cancelOrder(Long orderNo) {
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_no", orderNo);
        updateWrapper.set("status", OrderStatus.CANCELLED.getCode());
        updateWrapper.set("close_time", new Date());
        orderMapper.update(updateWrapper);

        //按订单明细把商品库存加回去
        LambdaQueryWrapper<OrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(OrderItem::getOrderNo, orderNo);
        List<OrderItem> orderItemList = orderItemMapper.selectList(itemQueryWrapper);
        for (OrderItem orderItem : orderItemList) {
            Result restoreResult = productClient.restoreStock(orderItem.getProductId(), orderItem.getQuantity());
            if (restoreResult.getCode() == Result.ERROR) {
                throw new ServiceException(restoreResult.getMsg());
            }
        }
    }

    //删除订单：订单和它的明细一起逻辑删除
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteById(Long orderNo) {
        Order dbOrder = orderMapper.selectById(orderNo);
        if (dbOrder == null) {
            throw new ServiceException("订单不存在");
        }
        orderMapper.deleteById(orderNo);
        LambdaQueryWrapper<OrderItem> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(OrderItem::getOrderNo, orderNo);
        orderItemMapper.delete(itemQueryWrapper);
    }
}