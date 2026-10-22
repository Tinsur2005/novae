package cn.tinsur.mall.controller;


import cn.tinsur.mall.pojo.entity.ActivityProduct;
import cn.tinsur.mall.service.IActivityProductService;
import cn.tinsur.mall.util.Result;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 秒杀活动商品表 前端控制器
 * </p>
 *
 * @author Tinsur
 * @since 2026-10-22
 */
@RestController
@RequestMapping("/activity-product")
public class ActivityProductController {

    @Autowired
    private IActivityProductService activityProductService;

    /**
     * 活动下全部秒杀商品（编辑回显）
     */
    @GetMapping("/{activityId}")
    public Result<List<ActivityProduct>> listByActivityId(@PathVariable Long activityId) {
        List<ActivityProduct> activityProductList = activityProductService.list(Wrappers.<ActivityProduct>lambdaQuery()
                .eq(ActivityProduct::getActivityId, activityId)
                .orderByAsc(ActivityProduct::getId));
        return Result.ok(activityProductList);
    }

    /**
     * 条件更新扣减活动配额库存
     * PUT /activity-product/deduct?activityId=1&productId=100&quantity=1
     * 供 order-service 秒杀订单落库后 Feign 回调
     */
    @PutMapping("/deduct")
    public Result deductStock(@RequestParam Long activityId, @RequestParam Long productId, @RequestParam Integer quantity) {
        boolean success = activityProductService.deductStock(activityId, productId, quantity);
        return success ? Result.ok("扣减库存成功") : Result.error("库存不足，扣减失败");
    }
}
