package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.constant.SeckillConstant;
import cn.tinsur.mall.constant.SeckillMqConstant;
import cn.tinsur.mall.exception.ServiceException;
import cn.tinsur.mall.pojo.entity.Activity;
import cn.tinsur.mall.pojo.entity.ActivityProduct;
import cn.tinsur.mall.pojo.message.SeckillOrderMessage;
import cn.tinsur.mall.pojo.query.SeckillQuery;
import cn.tinsur.mall.service.IActivityProductService;
import cn.tinsur.mall.service.IActivityService;
import cn.tinsur.mall.service.ISeckillService;
import cn.tinsur.mall.util.LoginContext;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 秒杀下单实现
 * 热路径只读写 Redis：判活动、判重、扣库存、读商品快照全部在 Redis 完成，不写任何数据库
 */
@Service
public class SeckillServiceImpl implements ISeckillService {

    /** Lua 脚本返回值：预扣成功 */
    private static final long SECKILL_SUCCESS = 1;
    /** Lua 脚本返回值：库存不足 */
    private static final long SECKILL_SOLD_OUT = 0;
    /** Lua 脚本返回值：重复下单 */
    private static final long SECKILL_DUPLICATE = -1;
    /** Lua 脚本返回值：秒杀商品不存在 */
    private static final long SECKILL_NOT_FOUND = -2;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private IActivityService activityService;
    @Autowired
    private IActivityProductService activityProductService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private DefaultRedisScript<Long> seckillScript;

    @PostConstruct
    public void init() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setLocation(new ClassPathResource(SeckillConstant.SECKILL_LUA_PATH));
        seckillScript.setResultType(Long.class);
    }

    @Override
    public Long seckill(SeckillQuery seckillQuery) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        if (userId == null) {
            throw new ServiceException("请先登录");
        }
        Long activityId = seckillQuery.getActivityId();
        Long productId = seckillQuery.getProductId();

        //1.校验活动已发布且在进行中
        checkActivity(activityId);

        //2.Lua 原子预扣（判重 + 扣库存一个脚本完成）
        //  必须用 StringRedisTemplate 执行：RedisTemplate 的 Jackson 序列化器会给参数加 JSON 引号，Lua 里 hash field 匹不上
        Long result = stringRedisTemplate.execute(seckillScript,
                List.of(SeckillConstant.STOCK_KEY + activityId, SeckillConstant.USER_KEY + activityId),
                String.valueOf(productId), String.valueOf(userId), "1");
        if (result == null) {
            throw new ServiceException("系统繁忙，请稍后重试");
        }
        long code = result;
        if (code == SECKILL_SOLD_OUT) {
            throw new ServiceException("已售罄，下次早点来");
        }
        if (code == SECKILL_DUPLICATE) {
            throw new ServiceException("您已抢购过该商品，每人限购一件");
        }
        if (code == SECKILL_NOT_FOUND) {
            throw new ServiceException("秒杀商品不存在");
        }

        //3.读商品快照（缓存丢失兜底查库，判空防NPE）
        ActivityProduct product = getProductSnapshot(activityId, productId);
        if (product == null) {
            throw new ServiceException("秒杀商品不存在");
        }

        //4.订单号预生成（雪花算法，与 orders 表主键规则一致）
        long orderNo = IdWorker.getId();

        //5.组装消息发 RabbitMQ，预扣成功立即返回订单号，订单落库交给 order-service
        SeckillOrderMessage message = new SeckillOrderMessage();
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setActivityId(activityId);
        message.setProductId(productId);
        message.setQuantity(1);
        message.setSeckillPrice(product.getSeckillPrice());
        message.setProductName(product.getProductName());
        message.setProductImage(product.getProductImage());
        rabbitTemplate.convertAndSend(SeckillMqConstant.SECKILL_TOPIC_EXCHANGE,
                SeckillMqConstant.SECKILL_ORDER_ROUTING_KEY, message);
        return orderNo;
    }

    /**
     * 校验活动：status == 1 且 startTime <= now <= endTime，否则拒绝
     * 缓存丢失兜底查库
     */
    private void checkActivity(Long activityId) {
        Activity activity = getActivity(activityId);
        Date now = new Date();
        if (activity == null || activity.getStatus() == null || activity.getStatus() != 1) {
            throw new ServiceException("活动不在进行中");
        }
        if (now.before(activity.getStartTime()) || now.after(activity.getEndTime())) {
            throw new ServiceException("活动不在进行中");
        }
    }

    private Activity getActivity(Long activityId) {
        Object value = redisTemplate.opsForValue().get(SeckillConstant.ACTIVITY_KEY + activityId);
        if (value instanceof Activity activity) {
            return activity;
        }
        //缓存丢失兜底查库
        return activityService.getById(activityId);
    }

    /**
     * 读商品快照：取秒杀价、名称、图片；缓存丢失兜底查库
     */
    private ActivityProduct getProductSnapshot(Long activityId, Long productId) {
        Object value = redisTemplate.opsForHash().get(SeckillConstant.PRODUCT_KEY + activityId,
                String.valueOf(productId));
        if (value instanceof ActivityProduct product) {
            return product;
        }
        //缓存丢失兜底查库
        return activityProductService.getOne(Wrappers.<ActivityProduct>lambdaQuery()
                .eq(ActivityProduct::getActivityId, activityId)
                .eq(ActivityProduct::getProductId, productId));
    }
}
