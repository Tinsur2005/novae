package cn.tinsur.mall.pojo.query;

import lombok.Data;

import java.util.Date;

@Data
public class OrderQuery {
    //订单号
    private Long orderNo;
    //用户id
    private Long userId;
    //订单状态
    private Integer status;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}