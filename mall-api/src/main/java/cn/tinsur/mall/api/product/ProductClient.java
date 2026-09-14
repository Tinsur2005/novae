package cn.tinsur.mall.api.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@FeignClient(value = "product-service")
public interface ProductClient {

    @GetMapping("/product/selectAllImage")
    Set<String> selectAllImage();
}
