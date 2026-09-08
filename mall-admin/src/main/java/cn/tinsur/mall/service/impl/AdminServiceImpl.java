package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.exception.ServiceException;
import cn.tinsur.mall.util.PasswordUtil;
import cn.tinsur.mall.mapper.AdminMapper;
import cn.tinsur.mall.pojo.entity.Admin;
import cn.tinsur.mall.pojo.query.AdminQuery;
import cn.tinsur.mall.service.IAdminService;
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
 *  服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-07
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {
    @Autowired
    private AdminMapper adminMapper;

    @Override
    public IPage<Admin> list(AdminQuery adminQuery) {
        IPage<Admin> page = new Page<>(adminQuery.getPage(), adminQuery.getLimit());
        LambdaQueryWrapper<Admin> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(adminQuery.getName()), Admin::getName, adminQuery.getName())
                .like(!ObjectUtils.isEmpty(adminQuery.getEmail()), Admin::getEmail, adminQuery.getEmail())
                .between(!ObjectUtils.isEmpty(adminQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(adminQuery.getEndCreateTime()), Admin::getCreateTime, adminQuery.getBeginCreateTime(), adminQuery.getEndCreateTime())
                .orderByDesc(Admin::getCreateTime);

        return adminMapper.selectPage(page, lambdaQueryWrapper);
    }

    @Override
    public void add(Admin admin) {
        Admin adminInDB = adminMapper.selectOne(new QueryWrapper<Admin>().eq("name", admin.getName()));
        if (adminInDB != null) {
            throw new ServiceException("用户名已存在");
        }
        admin.setPassword(PasswordUtil.hash(admin.getPassword()));
        adminMapper.insert(admin);
    }
}