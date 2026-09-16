package cn.tinsur.mall.api.product;

import cn.tinsur.mall.api.pojo.Product;
import cn.tinsur.mall.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

@FeignClient(value = "product-service")
public interface ProductClient {

    @GetMapping("/product/selectAllImage")
    Set<String> selectAllImage();

    @GetMapping("/product/{id}")
    Result<Product> selectById(@PathVariable Long id);
}