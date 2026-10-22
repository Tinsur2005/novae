package cn.tinsur.mall.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 秒杀活动商品表
 * </p>
 *
 * @author Tinsur
 * @since 2026-10-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ActivityProduct implements Serializable {


    /**
     * 活动商品记录ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 秒杀活动ID
     */
    @TableField("activity_id")
    private Long activityId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 秒杀价格
     */
    @TableField("seckill_price")
    private BigDecimal seckillPrice;

    /**
     * 活动初始库存
     */
    @TableField("total_stock")
    private Integer totalStock;

    /**
     * 数据库剩余库存
     */
    private Integer stock;

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
     * 商品普通价格快照
     */
    @TableField("product_price")
    private BigDecimal productPrice;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
