package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.common.Result;
import com.mental.entity.ExamRecord;
import com.mental.entity.User;
import com.mental.security.Access;
import com.mental.service.ExamRecordService;
import com.mental.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin
public class TeacherController {

    @Resource
    private UserService userService;

    @Resource
    private ExamRecordService examRecordService;

    /**
     * 老师查看学生统计信息
     */
    @GetMapping("/student-statistics")
    public Result studentStatistics() {
        Result r = Access.checkTeacher();
        if (r != null) return r;

        Map<String, Object> map = new HashMap<>();

        // 统计学生数量
        long studentCount = userService.count(
                new LambdaQueryWrapper<User>().eq(User::getRole, "USER")
        );
        map.put("studentCount", studentCount);

        // 统计考试记录
        long examCount = examRecordService.count();
        map.put("examCount", examCount);

        // 统计高风险学生
        List<ExamRecord> highRiskRecords = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getLevel, "重度心理风险")
        );
        map.put("highRiskCount", highRiskRecords.size());

        // 统计中等风险学生
        List<ExamRecord> mediumRiskRecords = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getLevel, "中度心理风险")
        );
        map.put("mediumRiskCount", mediumRiskRecords.size());

        return Result.success(map);
    }

    /**
     * 老师查看学生列表
     */
    @GetMapping("/students")
    public Result getStudents() {
        Result r = Access.checkTeacher();
        if (r != null) return r;

        List<User> students = userService.list(
                new LambdaQueryWrapper<User>().eq(User::getRole, "USER")
        );
        return Result.success(students);
    }

    /**
     * 老师查看学生考试记录
     */
    @GetMapping("/student-records/{studentId}")
    public Result getStudentRecords(@PathVariable Long studentId) {
        Result r = Access.checkTeacher();
        if (r != null) return r;

        List<ExamRecord> records = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getUserId, studentId)
        );
        return Result.success(records);
    }

    /**
     * 老师查看高风险学生列表
     */
    @GetMapping("/high-risk-students")
    public Result getHighRiskStudents() {
        Result r = Access.checkTeacher();
        if (r != null) return r;

        // 获取高风险考试记录
        List<ExamRecord> highRiskRecords = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getLevel, "重度心理风险")
        );

        // 获取对应的学生信息
        Map<Long, User> highRiskStudents = new HashMap<>();
        for (ExamRecord record : highRiskRecords) {
            User student = userService.getById(record.getUserId());
            if (student != null) {
                highRiskStudents.put(student.getId(), student);
            }
        }

        return Result.success(highRiskStudents.values());
    }

    /**
     * 老师个人信息
     */
    @GetMapping("/profile")
    public Result getProfile() {
        Result r = Access.checkTeacher();
        if (r != null) return r;

        // 这里需要从JWT中获取当前登录的老师ID
        // 暂时返回成功，实际实现需要获取当前用户信息
        return Result.success("老师个人信息接口");
    }
}