package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.api.category.CategoryClient;
import cn.tinsur.mall.mapper.ProductMapper;
import cn.tinsur.mall.pojo.entity.Product;
import cn.tinsur.mall.util.LoginContext;
import cn.tinsur.mall.pojo.query.ProductQuery;
import cn.tinsur.mall.pojo.vo.ProductVO;
import cn.tinsur.mall.service.IProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-09
 */
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public IPage<ProductVO> list(ProductQuery productQuery) {
        Long id = (Long) LoginContext.getLoginInfo().get("id");
        log.info("ProductServiceImpl list id: {}", id);

        IPage<Product> page = new Page<>(productQuery.getPage(), productQuery.getLimit());
        LambdaQueryWrapper<Product> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(productQuery.getName()), Product::getName, productQuery.getName())
                .like(!ObjectUtils.isEmpty(productQuery.getCategoryId()), Product::getCategoryId, productQuery.getCategoryId())
                //前台查询传status=1，只查上架商品；管理端不传则查全部
                .eq(!ObjectUtils.isEmpty(productQuery.getStatus()), Product::getStatus, productQuery.getStatus())
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

    //查询所有在用的图片(OSS对象名,可带目录前缀)，供定时任务清理OSS垃圾图片
    @Override
    public Set<String> selectAllImage() {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("main_image", "sub_images");
        List<Product> list = productMapper.selectList(queryWrapper);
        Set<String> set = new HashSet<>();
        for (Product product : list) {
            //主图
            collectOssKey(set, product.getMainImage());
            //副图，sub_images存的是JSON数组字符串
            if (!ObjectUtils.isEmpty(product.getSubImages())) {
                try {
                    List<String> urls = objectMapper.readValue(product.getSubImages(), new TypeReference<List<String>>() {
                    });
                    for (String url : urls) {
                        collectOssKey(set, url);
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
        return set;
    }

    //把图片URL转成OSS对象名，只统计阿里云OSS上的图片
    private void collectOssKey(Set<String> set, String url) {
        if (ObjectUtils.isEmpty(url) || !url.contains("aliyuncs.com")) {
            return;
        }
        //https://bucket.region.aliyuncs.com/product/xxx.png -> product/xxx.png
        String objectName = url.substring(url.indexOf("//") + 2);
        set.add(objectName.substring(objectName.indexOf("/") + 1));
    }
}
