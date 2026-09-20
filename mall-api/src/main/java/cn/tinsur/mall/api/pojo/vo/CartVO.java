package cn.tinsur.mall.api.pojo.vo;

import cn.tinsur.mall.api.pojo.entity.Product;
import lombok.Data;

@Data
public class CartVO {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer count;
    private Integer selected;

    private Product product;

    /*private String productName;
    private String productImage;
    private BigDecimal productPrice;*/
}