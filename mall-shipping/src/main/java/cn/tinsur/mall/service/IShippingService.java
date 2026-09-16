package cn.tinsur.mall.service;

import cn.tinsur.mall.pojo.entity.Shipping;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 收货地址表 服务类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-16
 */
public interface IShippingService extends IService<Shipping> {

    List<Shipping> listUserShipping();

    void add(Shipping shipping);

    void update(Shipping shipping);

    void setDefault(Long id);

    void deleteById(Long id);
}
