package cn.tinsur.mall.pojo.query;

import lombok.Data;

/**
 * 秒杀下单请求参数
 */
@Data
public class SeckillQuery {
    //秒杀活动ID
    private Long activityId;
    //商品ID
    private Long productId;
}
