package cn.tinsur.mall.api.product;

import cn.tinsur.mall.api.pojo.entity.Product;
import cn.tinsur.mall.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Set;

@FeignClient(value = "product-service")
public interface ProductClient {

    @GetMapping("/product/selectAllImage")
    Set<String> selectAllImage();

    @GetMapping("/product/{id}")
    Result<Product> selectById(@PathVariable Long id);

    /**
     * 扣减商品库存，库存不足时商品服务会返回失败
     */
    @PutMapping("/product/{id}/stock/deduct/{count}")
    Result deductStock(@PathVariable Long id, @PathVariable Integer count);

    /**
     * 回补商品库存，用于订单创建失败时补偿已经扣减的库存
     */
    @PutMapping("/product/{id}/stock/restore/{count}")
    Result restoreStock(@PathVariable Long id, @PathVariable Integer count);
}