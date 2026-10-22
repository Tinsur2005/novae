package cn.tinsur.mall.api.shipping;

import cn.tinsur.mall.api.pojo.entity.Shipping;
import cn.tinsur.mall.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "shipping-service")
public interface ShippingClient {

    /**
     * 根据ID查询收货地址，收货地址服务会校验只能查看自己的地址
     */
    @GetMapping("/shipping/{id}")
    Result<Shipping> selectById(@PathVariable Long id);
}
