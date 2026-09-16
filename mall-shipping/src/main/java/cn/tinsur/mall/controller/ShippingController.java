package cn.tinsur.mall.controller;


import cn.tinsur.mall.pojo.entity.Shipping;
import cn.tinsur.mall.service.IShippingService;
import cn.tinsur.mall.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 收货地址表 前端控制器
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-16
 */
@RestController
@RequestMapping("/shipping")
public class ShippingController {
    @Autowired
    private IShippingService shippingService;

    @GetMapping
    public Result<List<Shipping>> list() {
        List<Shipping> list = shippingService.listUserShipping();
        return Result.ok(list);
    }

}
