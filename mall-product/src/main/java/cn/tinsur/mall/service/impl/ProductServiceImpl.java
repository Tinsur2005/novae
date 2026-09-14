package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.api.category.CategoryClient;
import cn.tinsur.mall.mapper.ProductMapper;
import cn.tinsur.mall.pojo.entity.Product;
import cn.tinsur.mall.pojo.query.ProductQuery;
import cn.tinsur.mall.pojo.vo.ProductVO;
import cn.tinsur.mall.service.IProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-09
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryClient categoryClient;

    @Override
    public IPage<ProductVO> list(ProductQuery productQuery) {
        IPage<Product> page = new Page<>(productQuery.getPage(), productQuery.getLimit());
        LambdaQueryWrapper<Product> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(productQuery.getName()), Product::getName, productQuery.getName())
                .like(!ObjectUtils.isEmpty(productQuery.getCategoryId()), Product::getCategoryId, productQuery.getCategoryId())
                .between(!ObjectUtils.isEmpty(productQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(productQuery.getEndCreateTime()), Product::getCreateTime, productQuery.getBeginCreateTime(), productQuery.getEndCreateTime())
                .orderByDesc(Product::getCreateTime);
        productMapper.selectPage(page, lambdaQueryWrapper);

        List<Product> list = page.getRecords();
        List<ProductVO> productVOList = list.stream().map(product -> {
            ProductVO productVO = new ProductVO();
            BeanUtils.copyProperties(product, productVO);
            //远程调用获取分类名称
            String categoryName = categoryClient.selectNameById(product.getCategoryId());
            productVO.setCategoryName(categoryName);
            return productVO;
        }).toList();

        IPage<ProductVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(productVOList);
        return voPage;
    }

    //先从Redis缓存中读取商品，缓存中没有再查数据库并写入缓存
    @Cacheable(value = "productCache", key = "#id")
    @Override
    public Product selectById(Long id) {
        System.out.println("ProductServiceImpl.selectById");
        return productMapper.selectById(id);
    }

    //修改商品后删除缓存，下次查询时重新加载
    @CacheEvict(value = "productCache", key = "#product.id")
    @Override
    public void update(Product product) {
        productMapper.updateById(product);
    }

    //删除商品后删除缓存
    @CacheEvict(value = "productCache", key = "#id")
    @Override
    public void deleteById(Long id) {
        productMapper.deleteById(id);
    }

    //批量删除后商品缓存的key不确定，直接清空productCache缓存
    @CacheEvict(value = "productCache", allEntries = true)
    @Override
    public boolean removeByIds(Collection<?> list) {
        return super.removeByIds(list);
    }
}
