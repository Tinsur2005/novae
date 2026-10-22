package cn.tinsur.mall.service;

import cn.tinsur.mall.pojo.query.SeckillQuery;

/**
 * 秒杀下单服务
 */
public interface ISeckillService {

    /**
     * 秒杀下单（热路径只操作 Redis）：
     * 校验活动 → Lua 原子预扣（判重+扣库存）→ 读商品快照 → 预生成订单号 → 发 MQ
     *
     * @return 预生成的订单号（订单由 order-service 异步落库）
     */
    Long seckill(SeckillQuery seckillQuery);
}
