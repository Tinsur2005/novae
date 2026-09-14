package cn.tinsur.mall.pojo.vo;

import cn.tinsur.mall.pojo.entity.Product;
import lombok.Data;

@Data
public class ProductVO extends Product {
    //分类名字，关联category表查询出来
    private String categoryName;
}
