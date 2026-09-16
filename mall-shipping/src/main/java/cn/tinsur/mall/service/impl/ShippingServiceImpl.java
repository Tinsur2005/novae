package cn.tinsur.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.tinsur.mall.pojo.entity.Shipping;
import cn.tinsur.mall.mapper.ShippingMapper;
import cn.tinsur.mall.service.IShippingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.tinsur.mall.util.LoginContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 收货地址表 服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-16
 */
@Service
public class ShippingServiceImpl extends ServiceImpl<ShippingMapper, Shipping> implements IShippingService {
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public List<Shipping> listUserShipping() {
        Long id = (Long) LoginContext.getLoginInfo().get("id");
        LambdaQueryWrapper<Shipping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Shipping::getUserId, id);
        return shippingMapper.selectList(queryWrapper);
    }
}
