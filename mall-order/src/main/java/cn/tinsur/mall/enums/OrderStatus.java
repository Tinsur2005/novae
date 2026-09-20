package cn.tinsur.mall.enums;

/**
 * 订单状态，取值与orders表status字段的注释一致
 */
public enum OrderStatus {
    UNPAID(-1, "未付款"),
    CANCELLED(0, "已取消"),
    WAIT_SEND(1, "待发货"),
    SENT(2, "已发货"),
    SUCCESS(3, "交易成功"),
    CLOSED(4, "交易关闭"),
    REFUNDED(5, "已退款");

    private int code;
    private String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}