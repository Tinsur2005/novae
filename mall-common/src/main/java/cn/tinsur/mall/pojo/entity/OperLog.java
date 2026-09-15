package cn.tinsur.mall.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 操作日志表
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OperLog implements Serializable {


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模块名
     */
    private String module;

    /**
     * 日志类型
     */
    @TableField("log_type")
    private String logType;

    /**
     * 操作管理员id
     */
    @TableField("admin_id")
    private Long adminId;

    /**
     * 操作管理员名
     */
    @TableField("admin_name")
    private String adminName;

    /**
     * 请求方式GET/POST
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求URI
     */
    @TableField("request_uri")
    private String requestUri;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 返回参数
     */
    @TableField("response_params")
    private String responseParams;

    /**
     * 请求IP
     */
    @TableField("request_ip")
    private String requestIp;

    /**
     * 服务器地址
     */
    @TableField("server_address")
    private String serverAddress;

    /**
     * 是否异常:1-异常 0-正常
     */
    private Integer exception;

    /**
     * 异常信息
     */
    @TableField("exception_msg")
    private String exceptionMsg;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private Date endTime;

    /**
     * 执行耗时,毫秒
     */
    @TableField("execute_time")
    private Integer executeTime;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 操作系统
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 浏览器名
     */
    @TableField("browser_name")
    private String browserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;


}
