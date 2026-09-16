package cn.tinsur.mall.pojo.query;

import lombok.Data;

import java.util.Date;

@Data
public class UserQuery {
    private String name;
    private String email;
    private String phone;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}
