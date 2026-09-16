package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.exception.ServiceException;
import cn.tinsur.mall.pojo.entity.User;
import cn.tinsur.mall.mapper.UserMapper;
import cn.tinsur.mall.pojo.query.UserQuery;
import cn.tinsur.mall.service.IUserService;
import cn.tinsur.mall.util.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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
    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<User> list(UserQuery userQuery) {
        IPage<User> page = new Page<>(userQuery.getPage(), userQuery.getLimit());
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(userQuery.getName()), User::getName, userQuery.getName())
                .like(!ObjectUtils.isEmpty(userQuery.getEmail()), User::getEmail, userQuery.getEmail())
                .like(!ObjectUtils.isEmpty(userQuery.getPhone()), User::getPhone, userQuery.getPhone())
                .between(!ObjectUtils.isEmpty(userQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(userQuery.getEndCreateTime()), User::getCreateTime, userQuery.getBeginCreateTime(), userQuery.getEndCreateTime())
                .orderByDesc(User::getCreateTime);

        return userMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public void register(User user) {
        // 用户名、密码必填
        if (user.getName() == null || user.getName().isEmpty()
                || user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new ServiceException("用户名和密码不能为空");
        }
        // 用户名查重，照抄管理员新增的写法
        User userInDB = userMapper.selectOne(new QueryWrapper<User>().eq("name", user.getName()));
        if (userInDB != null) {
            throw new ServiceException("用户名已存在");
        }
        // 密码BCrypt加密后保存，新用户默认正常状态
        user.setPassword(PasswordUtil.hash(user.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);
    }
}
