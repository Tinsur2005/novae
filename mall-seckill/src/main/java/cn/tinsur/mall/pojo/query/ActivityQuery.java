package cn.tinsur.mall.pojo.query;

import lombok.Data;

/**
 * 活动分页查询条件
 */
@Data
public class ActivityQuery {
    //活动名称（模糊）
    private String name;
    //0未发布 1已发布
    private Integer status;
    private Integer page;
    private Integer limit;
}
