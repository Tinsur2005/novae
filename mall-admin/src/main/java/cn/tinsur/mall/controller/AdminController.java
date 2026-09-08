package cn.tinsur.mall.controller;

import cn.tinsur.mall.util.JwtUtil;
import cn.tinsur.mall.util.PasswordUtil;
import cn.tinsur.mall.util.Result;
import cn.tinsur.mall.pojo.dto.AdminPasswordDTO;
import cn.tinsur.mall.pojo.entity.Admin;
import cn.tinsur.mall.pojo.query.AdminQuery;
import cn.tinsur.mall.service.IAdminService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author Gao
 * @since 2026-08-24
 */
@RestController
@RequestMapping("/admins")
public class AdminController {
    @Autowired
    private IAdminService adminService;

    @PutMapping("/resetPassword")
    public Result resetPassword(@RequestHeader("Authorization") String token,
                                @RequestBody AdminPasswordDTO adminPasswordDTO) {
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        Admin admin = adminService.getById(id);
        if (!admin.getPassword().equalsIgnoreCase(adminPasswordDTO.getOldPassword())) {
            return Result.error("旧密码错误");
        }

        Admin updateAdmin = new Admin();
        updateAdmin.setId(admin.getId());
        updateAdmin.setPassword(adminPasswordDTO.getNewPassword());
        adminService.updateById(updateAdmin);
        return Result.ok("密码修改成功");

    }

    @GetMapping("/adminInfo")
    public Result<Admin> adminInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> map = JwtUtil.parseToken(token);
        Integer id = (Integer) map.get("id");
        Admin admin = adminService.getById(id);
        admin.setPassword(null);

        return Result.ok(admin);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody Admin admin) {
        // 根据用户名查找这个用户
        Admin dbAdmin = adminService.getOne(new QueryWrapper<Admin>().eq("name", admin.getName()));
        if (dbAdmin == null) {
            return Result.error("用户名不存在");
        }
        /*if (!dbAdmin.getPassword().equalsIgnoreCase(admin.getPassword())) {
            return Result.error("密码错误");
        }*/
        if (!PasswordUtil.matches(admin.getPassword(), dbAdmin.getPassword())) {
            return Result.error("密码错误");
        }
        // 登录成功后，判断用户是否被禁用
        if (dbAdmin.getStatus() == 0) {
            return Result.error("用户已禁用");
        }

        // 登录成功，生成token
        Map<String, Object> map = new HashMap<>();
        map.put("id", dbAdmin.getId());
        map.put("name", dbAdmin.getName());
        String token = JwtUtil.createToken(map);
        return Result.ok("登录成功", token);
    }

    /**
     * 分页查询用户列表
     * GET /admins?page=1&limit=10&name=xxx&phone=xxx
     */
    @GetMapping
    public Result<IPage<Admin>> list(AdminQuery adminQuery) {
        IPage<Admin> page = adminService.list(adminQuery);
        return Result.ok(page);
    }

    /**
     * 根据ID查询用户
     * GET /admins/1
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.ok(adminService.getById(id));
    }

    /**
     * 新增用户
     * POST /admins
     */
    @PostMapping
    public Result add(@RequestBody Admin admin) {
        adminService.add(admin);
        return Result.ok("新增成功");
    }

    /**
     * 修改用户
     * PUT /admins/1
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Admin admin) {
        admin.setId(id);
        adminService.updateById(admin);
        return Result.ok("修改成功");
    }

    /**
     * 根据ID删除用户（逻辑删除）
     * DELETE /admins/1
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        adminService.removeById(id);
        return Result.ok("删除成功");
    }

    /**
     * 批量删除用户
     * DELETE /admins
     */
    @DeleteMapping
    public Result deleteBatch(@RequestBody Long[] ids) {
        adminService.removeByIds(java.util.Arrays.asList(ids));
        return Result.ok("批量删除成功");
    }
}
