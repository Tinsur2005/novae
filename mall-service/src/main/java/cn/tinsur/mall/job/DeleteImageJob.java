package cn.tinsur.mall.job;

import cn.tinsur.mall.api.admin.AdminClient;
import cn.tinsur.mall.api.product.ProductClient;
import cn.tinsur.mall.util.AliOSSUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 定时清理阿里云OSS中没有被引用的垃圾图片
 * 流程：Feign汇总各微服务在用的图片 -> 列举OSS全部文件 -> 差集 -> 逐个删除
 * 注意：任何一个服务调用失败都会抛异常终止本次任务，不会误删图片
 */
@Configuration
@Slf4j
public class DeleteImageJob {
    @Autowired
    private ProductClient productClient;
    @Autowired
    private AdminClient adminClient;
    @Autowired
    private AliOSSUtil aliOSSUtil;

    //@Scheduled(cron = "0 0/2 * * * ?")
    @Scheduled(cron = "0 0 * * * ?")
    public void deleteImage() {
        log.info("DeleteImageJob.deleteImage");
        //1.查询所有微服务下面的图片
        Set<String> productImageSet = productClient.selectAllImage();
        Set<String> adminImageSet = adminClient.selectAllImage();
        Set<String> dbSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(productImageSet)) {
            dbSet.addAll(productImageSet);
        }
        if (!CollectionUtils.isEmpty(adminImageSet)) {
            dbSet.addAll(adminImageSet);
        }
        //2.查询阿里云OSS下面所有图片
        Set<String> ossSet = aliOSSUtil.listFile();
        if (!CollectionUtils.isEmpty(dbSet) && !CollectionUtils.isEmpty(ossSet)) {
            ossSet.removeAll(dbSet);
            for (String imageName : ossSet) {
                log.info("DeleteImageJob.deleteImage.imageName:{}", imageName);
                aliOSSUtil.deleteFile(imageName);
            }
        }
    }
}