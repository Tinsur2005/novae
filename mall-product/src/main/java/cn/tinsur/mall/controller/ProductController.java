package cn.tinsur.mall.controller;


import cn.tinsur.mall.annotation.MyLog;
import cn.tinsur.mall.pojo.entity.Product;
import cn.tinsur.mall.pojo.query.ProductQuery;
import cn.tinsur.mall.pojo.vo.ProductVO;
import cn.tinsur.mall.service.IProductService;
import cn.tinsur.mall.util.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Set;

/**
 * <p>
 * 商品 前端控制器
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-09
 */
@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private IProductService productService;

    /**
     * 分页查询商品列表
     * GET /product?page=1&limit=10&name=xxx&categoryId=xxx
     */
    @MyLog(module = "商品模块：查询")
    @GetMapping
    public Result<IPage<ProductVO>> list(ProductQuery productQuery) {
        IPage<ProductVO> page = productService.list(productQuery);
        return Result.ok(page);
    }

    /**
     * 根据ID查询商品
     * GET /product/1
     */
    @GetMapping("/{id}")
    public Result<Product> selectById(@PathVariable Long id) {
        Product product = productService.selectById(id);
        return Result.ok(product);
    }

    /**
     * 新增商品
     * POST /product
     */
    @MyLog(module = "商品模块：新增")
    @PostMapping
    public Result add(@RequestBody Product product) {
        productService.save(product);
        return Result.ok("添加成功");
    }

    /**
     * 修改商品
     * PUT /product/1
     */
    @MyLog(module = "商品模块：修改")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productService.update(product);
        return Result.ok("更新成功");
    }

    /**
     * 修改商品状态
     * PUT /product/1/status/0
     */
    @MyLog(module = "商品模块：状态修改")
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productService.update(product);
        return Result.ok("修改状态成功");
    }

    /**
     * 根据ID删除商品（逻辑删除）
     * DELETE /product/1
     */
    @MyLog(module = "商品模块：删除")
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        productService.deleteById(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量删除商品
     * DELETE /product
     */
    @MyLog(module = "商品模块：批量删除")
    @DeleteMapping
    public Result deleteAll(@RequestBody Long[] ids) {
        productService.removeByIds(Arrays.asList(ids));
        return Result.ok("批量删除成功");
    }

    /**
     * 查询所有在用的图片（供定时任务清理OSS垃圾图片）
     * GET /product/selectAllImage
     */
    @GetMapping("/selectAllImage")
    public Set<String> selectAllImage() {
        return productService.selectAllImage();
    }
}
