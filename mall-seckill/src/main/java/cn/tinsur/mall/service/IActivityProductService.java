package cn.tinsur.mall.service;

import cn.tinsur.mall.pojo.entity.ActivityProduct;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 秒杀活动商品表 服务类
 * </p>
 *
 * @author Tinsur
 * @since 2026-10-22
 */
public interface IActivityProductService extends IService<ActivityProduct> {

    /**
     * 条件更新扣减活动配额库存（stock >= quantity 才扣，数据库层面防超卖兜底）
     * 供 order-service 落库后 Feign 回调
     */
    boolean deductStock(Long activityId, Long productId, Integer quantity);
}
