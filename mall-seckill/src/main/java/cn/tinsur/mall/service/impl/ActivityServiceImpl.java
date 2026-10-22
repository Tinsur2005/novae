package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.constant.SeckillConstant;
import cn.tinsur.mall.exception.ServiceException;
import cn.tinsur.mall.mapper.ActivityMapper;
import cn.tinsur.mall.pojo.dto.ActivityDTO;
import cn.tinsur.mall.pojo.entity.Activity;
import cn.tinsur.mall.pojo.entity.ActivityProduct;
import cn.tinsur.mall.pojo.query.ActivityQuery;
import cn.tinsur.mall.service.IActivityProductService;
import cn.tinsur.mall.service.IActivityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 秒杀活动表 服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-10-22
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements IActivityService {

    @Autowired
    private IActivityProductService activityProductService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public IPage<Activity> list(ActivityQuery activityQuery) {
        IPage<Activity> page = new Page<>(activityQuery.getPage(), activityQuery.getLimit());
        LambdaQueryWrapper<Activity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(activityQuery.getName()), Activity::getName, activityQuery.getName())
                .eq(!ObjectUtils.isEmpty(activityQuery.getStatus()), Activity::getStatus, activityQuery.getStatus())
                .orderByDesc(Activity::getCreateTime);
        return page(page, lambdaQueryWrapper);
    }

    @Override
    public List<ActivityDTO> listPublished() {
        //已发布且未结束的活动（含未开始），按开始时间升序
        List<Activity> activityList = list(Wrappers.<Activity>lambdaQuery()
                .eq(Activity::getStatus, 1)
                .ge(Activity::getEndTime, new Date())
                .orderByAsc(Activity::getStartTime));

        List<ActivityDTO> activityDTOList = new ArrayList<>();
        for (Activity activity : activityList) {
            ActivityDTO activityDTO = new ActivityDTO();
            BeanUtils.copyProperties(activity, activityDTO);
            //商品快照读 product HASH，实时剩余库存读 stock HASH
            activityDTO.setProductList(getProductListWithStock(activity.getId()));
            activityDTOList.add(activityDTO);
        }
        return activityDTOList;
    }

    /**
     * 活动下商品列表：商品快照 + Redis 实时库存（快照里的 stock 是缓存时的值，不作展示依据）
     * 已知取舍：只读 Redis 无 DB 兜底，缓存丢失后活动展示为空商品
     */
    private List<ActivityProduct> getProductListWithStock(Long activityId) {
        Map<Object, Object> productMap = redisTemplate.opsForHash().entries(SeckillConstant.PRODUCT_KEY + activityId);
        Map<Object, Object> stockMap = stringRedisTemplate.opsForHash().entries(SeckillConstant.STOCK_KEY + activityId);
        List<ActivityProduct> productList = new ArrayList<>();
        productMap.forEach((field, value) -> {
            if (value instanceof ActivityProduct product) {
                Object stock = stockMap.get(field);
                if (stock != null) {
                    product.setStock(Integer.valueOf(stock.toString()));
                }
                productList.add(product);
            }
        });
        return productList;
    }

    @Override
    @Transactional
    public void add(ActivityDTO activityDTO) {
        checkActivityDTO(activityDTO);
        //1.保存活动（id 由雪花算法生成）
        Activity activity = new Activity();
        BeanUtils.copyProperties(activityDTO, activity);
        activity.setId(null);
        save(activity);
        //2.保存活动商品
        saveProductList(activity.getId(), activityDTO.getProductList());
        //3.缓存发布
        activityDTO.setId(activity.getId());
        cacheProductList(activityDTO);
    }

    @Override
    @Transactional
    public void update(Long id, ActivityDTO activityDTO) {
        checkActivityDTO(activityDTO);
        //1.更新活动
        Activity activity = new Activity();
        BeanUtils.copyProperties(activityDTO, activity);
        activity.setId(id);
        updateById(activity);
        //2.删除该活动原有商品后重新保存
        activityProductService.remove(Wrappers.<ActivityProduct>lambdaQuery()
                .eq(ActivityProduct::getActivityId, id));
        saveProductList(id, activityDTO.getProductList());
        //3.缓存重建（编辑即重新发布：库存重置、已购标记清空）
        activityDTO.setId(id);
        cacheProductList(activityDTO);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        //1.删除库中两表记录
        removeById(id);
        activityProductService.remove(Wrappers.<ActivityProduct>lambdaQuery()
                .eq(ActivityProduct::getActivityId, id));
        //2.删除全部缓存 key
        deleteCache(id);
    }

    /**
     * 保存活动商品行：id置null、绑定activityId、stock = totalStock
     */
    private void saveProductList(Long activityId, List<ActivityProduct> productList) {
        List<ActivityProduct> activityProductList = productList.stream().map(activityProduct -> {
            ActivityProduct product = new ActivityProduct();
            BeanUtils.copyProperties(activityProduct, product);
            product.setId(null);
            product.setActivityId(activityId);
            product.setStock(activityProduct.getTotalStock());
            return product;
        }).toList();
        activityProductService.saveBatch(activityProductList);
    }

    /**
     * 应用侧校验（uk_activity_product 唯一键是兜底）：
     * 时间合法、商品非空、活动内商品不重复、秒杀价/库存为正数
     */
    private void checkActivityDTO(ActivityDTO activityDTO) {
        if (activityDTO.getStartTime() == null || activityDTO.getEndTime() == null
                || !activityDTO.getStartTime().before(activityDTO.getEndTime())) {
            throw new ServiceException("活动时间不合法：开始时间必须早于结束时间");
        }
        List<ActivityProduct> productList = activityDTO.getProductList();
        if (ObjectUtils.isEmpty(productList)) {
            throw new ServiceException("请至少配置一个秒杀商品");
        }
        Set<Long> productIdSet = new HashSet<>();
        for (ActivityProduct product : productList) {
            if (product.getProductId() == null) {
                throw new ServiceException("秒杀商品ID不能为空");
            }
            if (!productIdSet.add(product.getProductId())) {
                throw new ServiceException("活动内商品不能重复");
            }
            if (product.getSeckillPrice() == null
                    || product.getSeckillPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("秒杀价必须大于0");
            }
            if (product.getTotalStock() == null || product.getTotalStock() <= 0) {
                throw new ServiceException("秒杀库存必须大于0");
            }
        }
    }

    @Override
    public void cacheProductList(ActivityDTO activityDTO) {
        Long activityId = activityDTO.getId();
        //1.先删除该活动全部 4 个缓存 key（先删后写，保证编辑后缓存与数据库一致，不留旧值）
        deleteCache(activityId);
        //2.草稿不进 Redis，对前台和下单链路天然不可见
        if (activityDTO.getStatus() == null || activityDTO.getStatus() != 1) {
            return;
        }
        //3.写活动 STRING
        Activity activity = new Activity();
        BeanUtils.copyProperties(activityDTO, activity);
        redisTemplate.opsForValue().set(SeckillConstant.ACTIVITY_KEY + activityId, activity);
        //4.写商品快照 HASH + 库存 HASH（库存重置为 total_stock）
        for (ActivityProduct product : activityDTO.getProductList()) {
            String field = String.valueOf(product.getProductId());
            redisTemplate.opsForHash().put(SeckillConstant.PRODUCT_KEY + activityId, field, product);
            stringRedisTemplate.opsForHash().put(SeckillConstant.STOCK_KEY + activityId,
                    field, String.valueOf(product.getTotalStock()));
        }
    }

    @Override
    public void deleteCache(Long activityId) {
        redisTemplate.delete(List.of(
                SeckillConstant.ACTIVITY_KEY + activityId,
                SeckillConstant.PRODUCT_KEY + activityId,
                SeckillConstant.STOCK_KEY + activityId,
                SeckillConstant.USER_KEY + activityId
        ));
    }
}
