package cn.tinsur.mall.pojo.query;

import lombok.Data;

import java.util.Date;

@Data
public class ProductQuery {
    private String name;
    //二级分类id
    private Long categoryId;
    //商品状态：0下架 1上架，前台查询传1只看上架商品，不传则查全部
    private Integer status;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}
