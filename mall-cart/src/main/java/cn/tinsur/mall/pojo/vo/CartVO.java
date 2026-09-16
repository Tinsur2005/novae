package cn.tinsur.mall.pojo.vo;

import cn.tinsur.mall.api.pojo.Product;
import cn.tinsur.mall.pojo.entity.Cart;
import lombok.Data;

@Data
public class CartVO extends Cart {
    /*private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer count;*/

    private Product product;

    /*private String productName;
    private String productImage;
    private BigDecimal productPrice;*/
}
