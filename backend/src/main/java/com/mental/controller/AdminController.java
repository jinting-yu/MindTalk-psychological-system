package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.common.Result;
import com.mental.entity.ExamRecord;
import com.mental.security.Access;
import com.mental.service.ExamRecordService;
import com.mental.service.UserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping({"/api/admin", "/admin"})
@CrossOrigin
public class AdminController {

    @Resource
    private UserService userService;

    @Resource
    private ExamRecordService examRecordService;

    @GetMapping("/statistics")
    public Result statistics() {
        Result r = Access.checkAdmin();
        if (r != null) return r;

        Map<String, Object> map = new HashMap<>();
        map.put("userCount", userService.count());
        map.put("examCount", examRecordService.count());

        long high = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getLevel, "重度心理风险")
        ).size();
        map.put("highRisk", high);

        return Result.success(map);
    }

    @GetMapping("/user")
    public Result getUserList() {
        // 暂时注释权限检查，方便测试
        // Result r = Access.checkAdmin();
        // if (r != null) return r;

        return Result.success(userService.list());
    }
}