package cn.tinsur.mall.pojo.dto;

import cn.tinsur.mall.pojo.entity.Activity;
import cn.tinsur.mall.pojo.entity.ActivityProduct;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 活动保存/回显 DTO：活动字段 + 活动下秒杀商品列表
 * 新增/编辑请求体；GET /activity/published 也用它响应（productList 的 stock 为 Redis 实时值）
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ActivityDTO extends Activity {

    //活动下秒杀商品列表
    private List<ActivityProduct> productList;
}
