package cn.tinsur.mall.controller;


import cn.tinsur.mall.annotation.MyLog;
import cn.tinsur.mall.pojo.entity.Order;
import cn.tinsur.mall.pojo.query.OrderQuery;
import cn.tinsur.mall.pojo.vo.OrderVO;
import cn.tinsur.mall.service.IOrderService;
import cn.tinsur.mall.util.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-18
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    /**
     * 提交订单，把购物车中已勾选的商品生成订单
     * POST /order
     */
    @MyLog(module = "订单模块：下单")
    @PostMapping
    public Result add(@RequestBody Order order) {
        orderService.add(order);
        return Result.ok("添加成功");
    }

    /**
     * 当前登录用户的订单列表，每项包含订单商品
     * GET /order
     */
    @GetMapping
    public Result<List<OrderVO>> list() {
        List<OrderVO> list = orderService.listAll();
        return Result.ok(list);
    }

    /**
     * 分页查询订单
     * GET /order/page?page=1&limit=10&orderNo=xxx&userId=1&status=-1
     */
    @MyLog(module = "订单模块：查询")
    @GetMapping("/page")
    public Result<IPage<OrderVO>> page(OrderQuery orderQuery) {
        IPage<OrderVO> page = orderService.page(orderQuery);
        return Result.ok(page);
    }

    /**
     * 发货
     * PUT /order/123456/send
     */
    @MyLog(module = "订单模块：发货")
    @PutMapping("/{orderNo}/send")
    public Result send(@PathVariable Long orderNo) {
        orderService.send(orderNo);
        return Result.ok("发货成功");
    }

    /**
     * 支付订单，虚拟支付，支付后变为待发货
     * PUT /order/123456/pay
     */
    @MyLog(module = "订单模块：支付")
    @PutMapping("/{orderNo}/pay")
    public Result pay(@PathVariable Long orderNo) {
        orderService.pay(orderNo);
        return Result.ok("支付成功");
    }

    /**
     * 关闭订单，下单时扣掉的库存会加回去
     * PUT /order/123456/close
     */
    @MyLog(module = "订单模块：关闭")
    @PutMapping("/{orderNo}/close")
    public Result close(@PathVariable Long orderNo) {
        orderService.close(orderNo);
        return Result.ok("关闭成功");
    }

    /**
     * 删除订单（逻辑删除，订单明细一起删）
     * DELETE /order/123456
     */
    @MyLog(module = "订单模块：删除")
    @DeleteMapping("/{orderNo}")
    public Result deleteById(@PathVariable Long orderNo) {
        orderService.deleteById(orderNo);
        return Result.ok("删除成功");
    }
}