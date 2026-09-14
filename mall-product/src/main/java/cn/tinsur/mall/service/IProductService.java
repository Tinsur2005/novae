package cn.tinsur.mall.service;

import cn.tinsur.mall.pojo.entity.Product;
import cn.tinsur.mall.pojo.query.ProductQuery;
import cn.tinsur.mall.pojo.vo.ProductVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-09
 */
public interface IProductService extends IService<Product> {

    IPage<ProductVO> list(ProductQuery productQuery);

    Product selectById(Long id);

    void update(Product product);

    void deleteById(Long id);

    Set<String> selectAllImage();
}
