package cn.tinsur.mall.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 订单明细表
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrderItem implements Serializable {


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    @TableField("order_no")
    private Long orderNo;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 商品id
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 商品名称快照
     */
    @TableField("product_name")
    private String productName;

    /**
     * 商品图片快照
     */
    @TableField("product_image")
    private String productImage;

    /**
     * 成交时商品单价快照,单位:元
     */
    @TableField("current_unit_price")
    private BigDecimal currentUnitPrice;

    /**
     * 购买数量(盲盒=抽数)
     */
    private Integer quantity;

    /**
     * 小计金额,单位:元
     */
    @TableField("total_price")
    private BigDecimal totalPrice;

    /**
     * 逻辑删除:1-已删 0-未删,随订单一起删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
