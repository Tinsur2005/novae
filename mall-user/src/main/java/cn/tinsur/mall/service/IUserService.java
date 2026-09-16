package cn.tinsur.mall.service;

import cn.tinsur.mall.pojo.entity.User;
import cn.tinsur.mall.pojo.query.UserQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-16
 */
public interface IUserService extends IService<User> {

    IPage<User> list(UserQuery userQuery);

    void register(User user);
}
