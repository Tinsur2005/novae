package cn.tinsur.mall.pojo.vo;

import cn.tinsur.mall.pojo.entity.Category;
import lombok.Data;

import java.util.List;

@Data
public class CategoryVO extends Category {
    //这个分类下面所有的子节点
    private List<CategoryVO> children;
}
