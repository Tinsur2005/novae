package cn.tinsur.mall.api.seckill;

import cn.tinsur.mall.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 秒杀服务 Feign 接口（order-service 落库后回调扣活动配额库存）
 */
@FeignClient(value = "seckill-service")
public interface SeckillClient {

    /**
     * 条件更新扣减活动配额库存（stock >= quantity 才扣，数据库层面防超卖兜底）
     */
    @PutMapping("/activity-product/deduct")
    Result deductStock(@RequestParam("activityId") Long activityId,
                       @RequestParam("productId") Long productId,
                       @RequestParam("quantity") Integer quantity);
}
