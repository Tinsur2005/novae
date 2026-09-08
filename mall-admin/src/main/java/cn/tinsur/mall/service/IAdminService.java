package cn.tinsur.mall.service;

import cn.tinsur.mall.pojo.entity.Admin;
import cn.tinsur.mall.pojo.query.AdminQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-07
 */
public interface IAdminService extends IService<Admin> {

    IPage<Admin> list(AdminQuery adminQuery);

    void add(Admin admin);
}