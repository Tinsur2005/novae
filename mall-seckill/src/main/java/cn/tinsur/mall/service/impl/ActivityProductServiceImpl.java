package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.mapper.ActivityProductMapper;
import cn.tinsur.mall.pojo.entity.ActivityProduct;
import cn.tinsur.mall.service.IActivityProductService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀活动商品表 服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-10-22
 */
@Service
public class ActivityProductServiceImpl extends ServiceImpl<ActivityProductMapper, ActivityProduct> implements IActivityProductService {

    /**
     * 条件更新扣减活动配额库存：stock >= quantity 才扣，数据库层面防超卖兜底
     * （防超卖主闸门是 Redis 预扣，此更新失败仅告警，最终一致）
     */
    @Override
    public boolean deductStock(Long activityId, Long productId, Integer quantity) {
        return update(Wrappers.<ActivityProduct>lambdaUpdate()
                .setSql("stock = stock - " + quantity)
                .eq(ActivityProduct::getActivityId, activityId)
                .eq(ActivityProduct::getProductId, productId)
                .ge(ActivityProduct::getStock, quantity));
    }
}
