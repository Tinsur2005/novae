package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.mapper.CategoryMapper;
import cn.tinsur.mall.pojo.entity.Category;
import cn.tinsur.mall.pojo.vo.CategoryVO;
import cn.tinsur.mall.service.ICategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-09
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    //先从Redis缓存中读取分类树，缓存中没有再查数据库并写入缓存
    @Cacheable(value = "categoryTree", key = "'tree'")
    @Override
    public List<CategoryVO> selectCategoryTree() {
        System.out.println("CategoryServiceImpl.selectCategoryTree");
        //1.查找所有分类，按排序升序
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        List<Category> categoryList = categoryMapper.selectList(lambdaQueryWrapper);
        //2.将categoryList转换成categoryVOList
        List<CategoryVO> categoryVOList = categoryList.stream().map(category -> {
            CategoryVO categoryVO = new CategoryVO();
            BeanUtils.copyProperties(category, categoryVO);
            return categoryVO;
        }).collect(Collectors.toList());

        List<CategoryVO> categoryVOTree = buildTree(categoryVOList);
        return categoryVOTree;
    }

    //更新类操作要删除缓存
    @CacheEvict(value = "categoryTree", key = "'tree'")
    @Override
    public void add(Category category) {
        categoryMapper.insert(category);
    }

    //修改分类后删除缓存，下次查询时重新加载
    @CacheEvict(value = "categoryTree", key = "'tree'")
    @Override
    public boolean updateById(Category category) {
        return super.updateById(category);
    }

    //删除分类后删除缓存，下次查询时重新加载
    @CacheEvict(value = "categoryTree", key = "'tree'")
    @Override
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    /**
     * 给我任何一个List<CategoryVO>，返回一个树形结构
     * @param categoryVOList
     * @return
     */
    public List<CategoryVO> buildTree(List<CategoryVO> categoryVOList) {
        //所有一级分类
        List<CategoryVO> categoryVOTree = categoryVOList.stream()
                .filter(categoryVO -> categoryVO.getParentId() == 0)
                .map(categoryVO -> {
                    categoryVO.setChildren(buildChildrenTree(categoryVO, categoryVOList)); // 构建children
                    return categoryVO;
                }).collect(Collectors.toList());
        return categoryVOTree;
    }

    /**
     * 构建子节点树
     * @param parentCategoryVO 父节点
     * @param categoryVOList  所有孩子集合
     * @return
     */
    private List<CategoryVO> buildChildrenTree(CategoryVO parentCategoryVO, List<CategoryVO> categoryVOList) {
        return categoryVOList.stream()
                .filter(categoryVO -> categoryVO.getParentId().equals(parentCategoryVO.getId()))
                .map(categoryVO -> {// 构建孩子节点的 children
                    categoryVO.setChildren(buildChildrenTree(categoryVO, categoryVOList)); // 递归构建children
                    return categoryVO;
                }).collect(Collectors.toList());
    }
}
