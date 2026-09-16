package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.pojo.entity.User;
import cn.tinsur.mall.mapper.UserMapper;
import cn.tinsur.mall.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-16
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
