package cn.tinsur.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import cn.tinsur.mall.exception.ServiceException;
import cn.tinsur.mall.pojo.entity.Shipping;
import cn.tinsur.mall.mapper.ShippingMapper;
import cn.tinsur.mall.service.IShippingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.tinsur.mall.util.LoginContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public void add(Shipping shipping) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        shipping.setUserId(userId);
        shippingMapper.insert(shipping);
    }

    @Override
    public void update(Shipping shipping) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        Shipping dbShipping = shippingMapper.selectById(shipping.getId());
        // 只能修改自己的收货地址
        if (dbShipping == null || !dbShipping.getUserId().equals(userId)) {
            throw new ServiceException("收货地址不存在");
        }
        // 用户id以登录信息为准，防止越权
        shipping.setUserId(userId);
        shippingMapper.updateById(shipping);
    }

    @Transactional
    @Override
    public void setDefault(Long id) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        Shipping dbShipping = shippingMapper.selectById(id);
        // 只能设置自己的收货地址
        if (dbShipping == null || !dbShipping.getUserId().equals(userId)) {
            throw new ServiceException("收货地址不存在");
        }
        // 先把该用户所有地址的默认标记清零
        LambdaUpdateWrapper<Shipping> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Shipping::getUserId, userId).set(Shipping::getIsDefault, 0);
        shippingMapper.update(null, updateWrapper);
        // 再把当前地址设为默认
        Shipping shipping = new Shipping();
        shipping.setId(id);
        shipping.setIsDefault(1);
        shippingMapper.updateById(shipping);
    }

    @Override
    public void deleteById(Long id) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        Shipping dbShipping = shippingMapper.selectById(id);
        // 只能删除自己的收货地址
        if (dbShipping == null || !dbShipping.getUserId().equals(userId)) {
            throw new ServiceException("收货地址不存在");
        }
        shippingMapper.deleteById(id);
    }
}