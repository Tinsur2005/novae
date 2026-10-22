package cn.tinsur.mall.constant;

/**
 * 秒杀 Redis key 常量（前缀 + activityId）
 */
public interface SeckillConstant {

    /** STRING：活动对象（含 status、开始结束时间），下单校验用 */
    String ACTIVITY_KEY = "seckill:activity:";

    /** HASH：field=productId，value=ActivityProduct 快照 */
    String PRODUCT_KEY = "seckill:product:";

    /** HASH：field=productId，value=剩余库存（Lua 原子预扣、前台实时展示） */
    String STOCK_KEY = "seckill:stock:";

    /** HASH：field=productId:userId，value=1，一人一单已购标记 */
    String USER_KEY = "seckill:user:";

    /** Lua 脚本 classpath 位置 */
    String SECKILL_LUA_PATH = "lua/seckill.lua";
}
