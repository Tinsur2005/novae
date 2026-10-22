package cn.tinsur.mall.service;

import cn.tinsur.mall.pojo.entity.Activity;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.tinsur.mall.pojo.dto.ActivityDTO;
import cn.tinsur.mall.pojo.query.ActivityQuery;

import java.util.List;

/**
 * <p>
 * 秒杀活动表 服务类
 * </p>
 *
 * @author Tinsur
 * @since 2026-10-22
 */
public interface IActivityService extends IService<Activity> {

    /**
     * 分页查询活动（后台管理）
     */
    IPage<Activity> list(ActivityQuery activityQuery);

    /**
     * 前台秒杀页：已发布且未结束的活动（含未开始）+ 商品快照 + Redis 实时库存
     */
    List<ActivityDTO> listPublished();

    /**
     * 新增活动+商品，status=1 时发布缓存
     */
    void add(ActivityDTO activityDTO);

    /**
     * 编辑活动：删旧商品重存 + 缓存重建（相当于重新发布）
     */
    void update(Long id, ActivityDTO activityDTO);

    /**
     * 删除活动+商品+全部缓存
     */
    void deleteById(Long id);

    /**
     * 缓存发布：先删 4 个缓存 key，status != 1（草稿）只删不写
     */
    void cacheProductList(ActivityDTO activityDTO);

    /**
     * 删除活动的 4 个缓存 key
     */
    void deleteCache(Long activityId);
}
