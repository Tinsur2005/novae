package cn.tinsur.mall.api.admin;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@FeignClient(value = "admin-service")
public interface AdminClient {

    @GetMapping("/admin/selectAllImage")
    Set<String> selectAllImage();
}