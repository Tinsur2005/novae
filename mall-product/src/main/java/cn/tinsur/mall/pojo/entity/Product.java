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
 * 商品表
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Product implements Serializable {


    /**
     * 商品id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类id
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 商品类型:1-普通商品 2-盲盒(创建盲盒时与box_series同步写入)
     */
    private Integer type;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品副标题
     */
    private String subtitle;

    /**
     * 商品主图url
     */
    @TableField("main_image")
    private String mainImage;

    /**
     * 轮播图url列表,json数组
     */
    @TableField("sub_images")
    private String subImages;

    /**
     * 商品详情,富文本
     */
    private String detail;

    /**
     * 单价,单位:元
     */
    private BigDecimal price;

    /**
     * 库存数量(盲盒系列=盒数)
     */
    private Integer stock;

    /**
     * 状态:1-在售 0-下架
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
