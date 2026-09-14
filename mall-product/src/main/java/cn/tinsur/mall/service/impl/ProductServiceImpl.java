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
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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
}
