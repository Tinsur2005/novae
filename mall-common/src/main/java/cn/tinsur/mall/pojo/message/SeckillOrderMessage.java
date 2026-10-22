package cn.tinsur.mall.pojo.message;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀下单消息（seckill-service 发送，order-service 消费）
 * 消息类必须两侧 classpath 一致（放在 mall-common），Jackson 按 __TypeId__ 反序列化
 */
@Data
public class SeckillOrderMessage implements Serializable {

    /** 预生成订单号（雪花算法，发送方生成，作为 orders 主键，兼作幂等键） */
    private Long orderNo;

    /** 下单用户 */
    private Long userId;

    /** 秒杀活动ID */
    private Long activityId;

    /** 商品ID */
    private Long productId;

    /** 购买数量（秒杀固定 1） */
    private Integer quantity;

    /** 秒杀价快照（落库不回查商品） */
    private BigDecimal seckillPrice;

    /** 商品名称快照 */
    private String productName;

    /** 商品图片快照 */
    private String productImage;
}
