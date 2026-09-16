package cn.tinsur.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.tinsur.mall.api.pojo.Product;
import cn.tinsur.mall.api.product.ProductClient;
import cn.tinsur.mall.pojo.entity.Cart;
import cn.tinsur.mall.mapper.CartMapper;
import cn.tinsur.mall.pojo.vo.CartVO;
import cn.tinsur.mall.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.tinsur.mall.exception.ServiceException;
import cn.tinsur.mall.util.LoginContext;
import cn.tinsur.mall.util.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-16
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductClient productClient;

    @Override
    public void add(Cart cart) {
        Long id = (Long) LoginContext.getLoginInfo().get("id");
        cart.setUserId(id);
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, id);
        queryWrapper.eq(Cart::getProductId, cart.getProductId());
        Cart dbCart = cartMapper.selectOne(queryWrapper);
        // 如果数据库中存在该商品，则更新数量
        if (dbCart != null) {
            dbCart.setCount(dbCart.getCount() + cart.getCount());
            cartMapper.updateById(dbCart);
        } else {
            cartMapper.insert(cart);
        }
    }

    @Override
    public List<CartVO> listAll() {
        Long id = (Long) LoginContext.getLoginInfo().get("id");
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, id);
        List<Cart> cartList = cartMapper.selectList(queryWrapper);
        List<CartVO> cartVOList = cartList.stream().map(cart -> {
            CartVO cartVO = new CartVO();
            BeanUtils.copyProperties(cart, cartVO);
            //远程调用商品服务，获取商品信息
            Result<Product> result = productClient.selectById(cart.getProductId());
            if (result.getCode() == Result.OK) {
                Product product = result.getData();
                cartVO.setProduct(product);
            }
            return cartVO;
        }).collect(Collectors.toList());

        return cartVOList;
    }

    @Override
    public void update(Cart cart) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        Cart dbCart = cartMapper.selectById(cart.getId());
        // 只能修改自己购物车里的商品
        if (dbCart == null || !dbCart.getUserId().equals(userId)) {
            throw new ServiceException("购物车项不存在");
        }
        // 只允许修改数量和勾选状态
        Cart updateCart = new Cart();
        updateCart.setId(cart.getId());
        updateCart.setCount(cart.getCount());
        updateCart.setSelected(cart.getSelected());
        cartMapper.updateById(updateCart);
    }

    @Override
    public void deleteById(Long id) {
        Long userId = (Long) LoginContext.getLoginInfo().get("id");
        Cart dbCart = cartMapper.selectById(id);
        // 只能删除自己购物车里的商品
        if (dbCart == null || !dbCart.getUserId().equals(userId)) {
            throw new ServiceException("购物车项不存在");
        }
        cartMapper.deleteById(id);
    }
}
