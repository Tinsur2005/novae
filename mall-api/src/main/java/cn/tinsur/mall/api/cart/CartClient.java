package cn.tinsur.mall.api.cart;

import cn.tinsur.mall.api.pojo.vo.CartVO;
import cn.tinsur.mall.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "cart-service")
public interface CartClient {

    @GetMapping("/cart")
    Result<List<CartVO>> list();

    @DeleteMapping("/cart/{id}")
    Result deletedById(@PathVariable Long id);
}