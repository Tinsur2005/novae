package cn.tinsur.mall.controller;


import cn.tinsur.mall.pojo.dto.ActivityDTO;
import cn.tinsur.mall.pojo.entity.Activity;
import cn.tinsur.mall.pojo.query.ActivityQuery;
import cn.tinsur.mall.service.IActivityService;
import cn.tinsur.mall.util.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 秒杀活动表 前端控制器
 * </p>
 *
 * @author Tinsur
 * @since 2026-10-22
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private IActivityService activityService;

    /**
     * 分页查询活动列表（后台管理）
     * GET /activity?page=1&limit=10&name=xxx&status=0
     */
    @GetMapping
    public Result<IPage<Activity>> list(ActivityQuery activityQuery) {
        IPage<Activity> page = activityService.list(activityQuery);
        return Result.ok(page);
    }

    /**
     * 前台秒杀页：已发布且未结束的活动（含未开始）+ 商品 + Redis 实时库存
     */
    @GetMapping("/published")
    public Result<List<ActivityDTO>> listPublished() {
        List<ActivityDTO> activityDTOList = activityService.listPublished();
        return Result.ok(activityDTOList);
    }

    /**
     * 根据ID查询活动（编辑回显）
     */
    @GetMapping("/{id}")
    public Result<Activity> selectById(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        return Result.ok(activity);
    }

    /**
     * 新增活动+商品（ActivityDTO），status=1 时发布缓存
     */
    @PostMapping
    public Result add(@RequestBody ActivityDTO activityDTO) {
        activityService.add(activityDTO);
        return Result.ok("添加成功");
    }

    /**
     * 编辑活动：删旧商品重存 + 缓存重建（相当于重新发布）
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody ActivityDTO activityDTO) {
        activityService.update(id, activityDTO);
        return Result.ok("更新成功");
    }

    /**
     * 删除活动+商品+缓存
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        activityService.deleteById(id);
        return Result.ok("删除成功");
    }
}
