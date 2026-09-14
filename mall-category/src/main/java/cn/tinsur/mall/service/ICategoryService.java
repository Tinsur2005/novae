package cn.tinsur.mall.service;

import cn.tinsur.mall.pojo.entity.Category;
import cn.tinsur.mall.pojo.vo.CategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-09
 */
public interface ICategoryService extends IService<Category> {

    List<CategoryVO> selectCategoryTree();
}
