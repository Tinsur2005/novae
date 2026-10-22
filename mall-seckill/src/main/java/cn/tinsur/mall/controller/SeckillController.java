package cn.tinsur.mall.controller;

import cn.tinsur.mall.pojo.query.SeckillQuery;
import cn.tinsur.mall.service.ISeckillService;
import cn.tinsur.mall.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒杀下单 前端控制器
 * 需登录：userId 由网关透传的 X-Login-Id 提供
 */
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private ISeckillService seckillService;

    /**
     * 秒杀下单
     * POST /seckill  body: {"activityId":1, "productId":100}
     * 预扣成功返回订单号；错误：活动不在进行中 / 已售罄 / 每人限购一件 / 秒杀商品不存在
     */
    @PostMapping
    public Result<Long> seckill(@RequestBody SeckillQuery seckillQuery) {
        Long orderNo = seckillService.seckill(seckillQuery);
        return Result.ok(orderNo);
    }
}
