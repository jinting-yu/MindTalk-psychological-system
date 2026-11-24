package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.common.Result;
import com.mental.entity.User;
import com.mental.security.UserContext;
import com.mental.service.ExamRecordService;
import com.mental.service.UserService;
import com.mental.util.JwtUtil;
import com.mental.util.Md5Util;
import com.mental.vo.ExamRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    @Autowired
    private ExamRecordService examRecordService;

    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        System.out.println("收到注册请求: " + user);

        // 如果 username 为 null，尝试从其他字段获取或生成默认值
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            // 如果 phone 存在，使用 phone 作为 username
            if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                user.setUsername(user.getPhone());
            }
            // 如果 realName 存在，使用 realName
            else if (user.getRealName() != null && !user.getRealName().trim().isEmpty()) {
                user.setUsername(user.getRealName());
            }
            // 否则生成随机用户名
            else {
                user.setUsername("user_" + System.currentTimeMillis());
            }
            System.out.println("自动生成用户名: " + user.getUsername());
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userService.getOne(wrapper) != null) {
            return Result.error("用户名已存在");
        }

        user.setPassword(Md5Util.md5(user.getPassword()));
        user.setRole("USER");
        userService.save(user);
        return Result.success("注册成功");
    }

    /**
     * 添加用户（管理员用）
     */
    /**
     * 管理员添加用户
     */
    /**
     * 管理员添加用户
     */
    @PostMapping("/add")
    public Result addUser(@RequestBody Map<String, Object> params) {
        System.out.println("收到添加用户请求: " + params);

        User user = new User();

        // 字段映射：前端字段 -> 后端字段
        // 前端 userAccount -> 后端 username
        String username = (String) params.get("userAccount");
        if (username == null) {
            username = (String) params.get("username");
        }
        user.setUsername(username);

        // 前端 userName -> 后端 realName
        String realName = (String) params.get("userName");
        if (realName == null) {
            realName = (String) params.get("realName");
        }
        user.setRealName(realName);

        // 密码
        String password = (String) params.get("userPassword");
        if (password == null) {
            password = (String) params.get("password");
        }
        user.setPassword(password);

        // 角色
        String role = (String) params.get("userRole");
        if (role == null) {
            role = (String) params.get("role");
        }
        user.setRole(role != null ? role : "USER");

        // 手机号（可选）
        String phone = (String) params.get("userProfile");
        if (phone == null) {
            phone = (String) params.get("phone");
        }
        user.setPhone(phone);

        // 验证必填字段
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userService.getOne(wrapper) != null) {
            return Result.error("用户名已存在");
        }

        // 加密密码
        user.setPassword(Md5Util.md5(user.getPassword()));

        // 设置创建时间
        user.setCreateTime(new Date());

        userService.save(user);
        return Result.success("添加成功");
    }

    @PostMapping("/login")
    public Result login(@RequestBody UserLoginVO loginVO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginVO.getUsername());
        User dbUser = userService.getOne(wrapper);
        if (dbUser == null) {
            return Result.error("用户不存在");
        }
        if (!dbUser.getPassword().equals(Md5Util.md5(loginVO.getPassword()))) {
            return Result.error("密码错误");
        }
        String token = jwtUtil.createToken(dbUser.getId(), dbUser.getUsername(), dbUser.getRole());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", dbUser);
        return Result.success(map);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/profile")
    public Result getProfile() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        User user = userService.getById(userId);
        // 清除密码信息
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }
    /**
     * 删除用户（管理员用）
     */
    /**
     * 删除用户
     * POST 方式：/api/user/delete
     */
    @PostMapping("/delete")
    public Result deleteUser(@RequestBody Map<String, String> params) {
        String idStr = params.get("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            return Result.error("用户ID不能为空");
        }

        try {
            Long id = Long.parseLong(idStr);
            // 不能删除自己
            Long currentUserId = UserContext.getUserId();
            if (currentUserId != null && currentUserId.equals(id)) {
                return Result.error("不能删除当前登录的用户");
            }

            boolean success = userService.removeById(id);
            return success ? Result.success("删除成功") : Result.error("用户不存在");
        } catch (NumberFormatException e) {
            return Result.error("用户ID格式错误");
        }
    }

    /**
     * 获取用户最新测评记录
     */
    @GetMapping("/latestExamRecords")
    public Result<List<ExamRecordVO>> getLatestExamRecords(@RequestParam(defaultValue = "3") int limit) {
        Long userId = UserContext.getUserId();
        List<ExamRecordVO> records = examRecordService.getLatestExamRecords(userId, limit);
        return Result.success(records);
    }

    /**
     * 更新个人资料
     */
    @PostMapping("/update-profile")
    public Result updateProfile(@RequestBody User user) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        // 确保只能更新自己的信息
        if (!user.getId().equals(userId)) {
            return Result.error("无权限修改其他用户信息");
        }

        // 如果更新密码，需要加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(Md5Util.md5(user.getPassword()));
        } else {
            // 如果密码为空，不更新密码
            User existingUser = userService.getById(userId);
            user.setPassword(existingUser.getPassword());
        }

        userService.updateById(user);
        // 返回更新后的用户信息（清除密码）
        User updatedUser = userService.getById(userId);
        updatedUser.setPassword(null);
        return Result.success(updatedUser);
    }

    @PostMapping("/update")
    public Result update(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result logout() {
        // JWT是无状态的，退出登录主要在前端清除token
        // 这里可以添加一些服务端的清理逻辑，如黑名单机制等
        return Result.success("退出登录成功");
    }
}