package cn.tinsur.mall.pojo.query;

import lombok.Data;

import java.util.Date;

@Data
public class ProductQuery {
    private String name;
    //二级分类id
    private Long categoryId;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}
