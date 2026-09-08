package cn.tinsur.mall.pojo.dto;

import lombok.Data;

@Data
public class AdminPasswordDTO {
    private String oldPassword;
    private String newPassword;
}