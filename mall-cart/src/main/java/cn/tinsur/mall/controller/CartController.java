package cn.tinsur.mall.controller;


import cn.tinsur.mall.pojo.entity.Cart;
import cn.tinsur.mall.pojo.vo.CartVO;
import cn.tinsur.mall.service.ICartService;
import cn.tinsur.mall.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 购物车表 前端控制器
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-16
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private ICartService cartService;

    @GetMapping
    public Result<List<CartVO>> list() {
        return Result.ok(cartService.listAll());
    }

    @PostMapping
    public Result add(@RequestBody Cart cart) {
        cartService.add(cart);
        return Result.ok("添加成功");
    }

    @PutMapping
    public Result update(@RequestBody Cart cart) {
        cartService.update(cart);
        return Result.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        cartService.deleteById(id);
        return Result.ok("删除成功");
    }

}
