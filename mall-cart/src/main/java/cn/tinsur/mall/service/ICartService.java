package cn.tinsur.mall.service;

import cn.tinsur.mall.pojo.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.tinsur.mall.pojo.vo.CartVO;

import java.util.List;

/**
 * <p>
 * 购物车表 服务类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-16
 */
public interface ICartService extends IService<Cart> {

    void add(Cart cart);

    List<CartVO> listAll();

    void update(Cart cart);

    void deleteById(Long id);
}
