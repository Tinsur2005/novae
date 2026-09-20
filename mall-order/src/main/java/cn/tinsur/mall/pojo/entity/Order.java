package cn.tinsur.mall.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-18
 */
@Data
@TableName("orders")
@EqualsAndHashCode(callSuper = false)
public class Order implements Serializable {


    /**
     * 订单号,主键,雪花算法或时间戳+随机数生成,全局唯一
     */
    @TableId(value = "order_no", type = IdType.ASSIGN_ID)
    private Long orderNo;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 收货地址id
     */
    @TableField("shipping_id")
    private Long shippingId;

    /**
     * 实付金额,单位:元
     */
    private BigDecimal payment;

    /**
     * 支付方式:1-微信 2-支付宝 3-其他
     */
    @TableField("payment_type")
    private Integer paymentType;

    /**
     * 运费,单位:元
     */
    private BigDecimal postage;

    /**
     * 支付时间
     */
    @TableField("payment_time")
    private Date paymentTime;

    /**
     * 发货时间
     */
    @TableField("send_time")
    private Date sendTime;

    /**
     * 交易完成时间
     */
    @TableField("end_time")
    private Date endTime;

    /**
     * 交易关闭时间
     */
    @TableField("close_time")
    private Date closeTime;

    /**
     * 订单状态:-1-未付款 0-已取消 1-待发货 2-已发货 3-交易成功 4-交易关闭 5-已退款
     */
    private Integer status;

    /**
     * 逻辑删除:1-已删 0-未删
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
