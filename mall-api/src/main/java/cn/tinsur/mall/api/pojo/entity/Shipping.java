package cn.tinsur.mall.api.pojo.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Shipping implements Serializable {
    private Long id;
    private Long userId;
    private String receiverName;
    private String receiverMobile;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private Integer isDefault;
}
