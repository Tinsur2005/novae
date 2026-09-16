package cn.tinsur.mall.controller;


import cn.tinsur.mall.pojo.entity.Shipping;
import cn.tinsur.mall.service.IShippingService;
import cn.tinsur.mall.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @PostMapping
    public Result add(@RequestBody Shipping shipping) {
        shippingService.add(shipping);
        return Result.ok("添加成功");
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Shipping shipping) {
        shipping.setId(id);
        shippingService.update(shipping);
        return Result.ok("更新成功");
    }

    @PutMapping("/{id}/default")
    public Result setDefault(@PathVariable Long id) {
        shippingService.setDefault(id);
        return Result.ok("设置默认地址成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        shippingService.deleteById(id);
        return Result.ok("删除成功");
    }

}
