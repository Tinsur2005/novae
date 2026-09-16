package cn.tinsur.mall.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import cn.tinsur.mall.annotation.MyLog;
import cn.tinsur.mall.pojo.entity.User;
import cn.tinsur.mall.pojo.query.UserQuery;
import cn.tinsur.mall.service.IUserService;
import cn.tinsur.mall.util.JwtUtil;
import cn.tinsur.mall.util.LoginContext;
import cn.tinsur.mall.util.PasswordUtil;
import cn.tinsur.mall.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-16
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private IUserService userService;

    @GetMapping("/userInfo")
    public Result<User> userInfo() {
        Map<String, Object> map = LoginContext.getLoginInfo();
        if (map == null) {
            return Result.error("用户未登录");
        }
        Long id = (Long) map.get("id");
        User user = userService.getById(id);
        user.setPassword(null);

        return Result.ok(user);
    }

    @MyLog(module = "注册")
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.ok("注册成功");
    }

    @MyLog(module = "登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        // 根据用户名查找这个用户
        User dbUser = userService.getOne(new QueryWrapper<User>().eq("name", user.getName()));
        if (dbUser == null) {
            return Result.error("用户名不存在");
        }
        if (!PasswordUtil.matches(user.getPassword(), dbUser.getPassword())) {
            return Result.error("密码错误");
        }
        // 登录成功后，判断用户是否被禁用
        if (dbUser.getStatus() == 0) {
            return Result.error("用户已禁用");
        }

        // 登录成功，生成token
        Map<String, Object> map = new HashMap<>();
        map.put("id", dbUser.getId());
        map.put("name", dbUser.getName());
        String token = JwtUtil.createToken(map);
        return Result.ok("登录成功", token);
    }

    @PutMapping
    public Result update(@RequestBody User user) {
        Long id = (Long) LoginContext.getLoginInfo().get("id");
        user.setId(id);
        userService.updateById(user);
        return Result.ok("更新成功");
    }

    /**
     * 分页查询用户列表
     * GET /user?page=1&limit=10&name=xxx&email=xxx&phone=xxx
     */
    @MyLog(module = "用户模块：查询")
    @GetMapping
    public Result<IPage<User>> list(UserQuery userQuery) {
        IPage<User> page = userService.list(userQuery);
        return Result.ok(page);
    }

    /**
     * 修改用户状态（启用/禁用）
     * PUT /user/1/status/0
     */
    @MyLog(module = "用户模块：状态修改")
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userService.updateById(user);
        return Result.ok("修改状态成功");
    }

    /**
     * 根据ID删除用户（逻辑删除）
     * DELETE /user/1
     */
    @MyLog(module = "用户模块：删除")
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        userService.removeById(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量删除用户
     * DELETE /user
     */
    @MyLog(module = "用户模块：批量删除")
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids) {
        userService.removeByIds(Arrays.asList(ids));
        return Result.ok("批量删除成功");
    }
}