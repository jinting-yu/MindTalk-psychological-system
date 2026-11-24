package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.common.Result;
import com.mental.entity.ExamRecord;
import com.mental.entity.Report;
import com.mental.security.UserContext;
import com.mental.service.ExamRecordService;
import com.mental.service.ReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@CrossOrigin
public class ReportController {

    @Resource
    private ReportService reportService;

    @Resource
    private ExamRecordService examRecordService;

    // ===================== 修复：根据 examId 获取报告（前端必须调用这个）=====================
    @GetMapping("/get/{examId}")
    public Result getReport(@PathVariable Long examId) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getExamId, examId);
        Report report = reportService.getOne(wrapper);
        if (report == null) {
            return Result.error("报告未生成");
        }
        return Result.success(report);
    }

    @GetMapping("/create/{examId}")
    public Result create(@PathVariable Long examId) {
        ExamRecord exam = examRecordService.getById(examId);
        if (exam == null) return Result.error("记录不存在");
        if (!exam.getUserId().equals(UserContext.getUserId())) return Result.error("无权限");

        String content;
        if (exam.getScaleId() == 2) {
            content = generateSASReport(exam);
        } else if (exam.getScaleId() == 3) {
            content = generateSDSReport(exam);
        } else if (exam.getScaleId() == 4) {
            content = generateInterpersonalReport(exam);
        } else if (exam.getScaleId() == 5) {
            content = generateEPQReport(exam);
        } else if (exam.getScaleId() == 6) {
            content = generateStressCopingReport(exam);
        } else {
            content = "【心理健康报告】\n总分：" + exam.getTotalScore() +
                    "\n等级：" + exam.getLevel() +
                    "\n风险：" + exam.getRisk() +
                    "\n\n建议：规律作息、运动、沟通、必要时咨询。";
        }

        Report report = new Report();
        report.setUserId(exam.getUserId());
        report.setExamId(examId);
        report.setContent(content);
        reportService.save(report);
        return Result.success(report);
    }

    @GetMapping("/my")
    public Result my() {
        List<Report> list = reportService.list(
                new LambdaQueryWrapper<Report>().eq(Report::getUserId, UserContext.getUserId())
        );
        return Result.success(list);
    }

    private String generateSASReport(ExamRecord exam) {
        StringBuilder content = new StringBuilder();
        content.append("【焦虑自评量表(SAS)报告】\n\n");
        content.append("测评时间：").append(exam.getCreateTime()).append("\n");
        content.append("总分：").append(exam.getTotalScore()).append("\n");
        content.append("标准分：").append(exam.getTotalScore() * 1.25).append("\n");
        content.append("等级：").append(exam.getLevel()).append("\n");
        content.append("风险评估：").append(exam.getRisk()).append("\n\n");
        content.append("【建议】\n");
        if (exam.getLevel().contains("正常")) {
            content.append("您的焦虑水平正常，请继续保持良好的心理状态。");
        } else if (exam.getLevel().contains("轻度")) {
            content.append("您存在轻度焦虑情绪，建议学习放松技巧，如深呼吸、冥想等。");
        } else if (exam.getLevel().contains("中度")) {
            content.append("您存在中度焦虑情绪，建议寻求心理咨询师的帮助。");
        } else {
            content.append("您存在重度焦虑情绪，强烈建议立即寻求专业心理医生的帮助。");
        }
        return content.toString();
    }

    private String generateSDSReport(ExamRecord exam) {
        StringBuilder content = new StringBuilder();
        content.append("【抑郁自评量表(SDS)报告】\n\n");
        content.append("测评时间：").append(exam.getCreateTime()).append("\n");
        content.append("总分：").append(exam.getTotalScore()).append("\n");
        content.append("标准分：").append(exam.getTotalScore() * 1.25).append("\n");
        content.append("等级：").append(exam.getLevel()).append("\n");
        content.append("风险评估：").append(exam.getRisk()).append("\n\n");
        content.append("【建议】\n");
        if (exam.getLevel().contains("正常")) {
            content.append("您的情绪状态良好，请继续保持积极的生活态度。");
        } else if (exam.getLevel().contains("轻度")) {
            content.append("您存在轻度抑郁情绪，建议多参加户外活动，与朋友交流，保持规律作息。");
        } else if (exam.getLevel().contains("中度")) {
            content.append("您存在中度抑郁情绪，建议寻求心理咨询师的帮助，必要时就医。");
        } else {
            content.append("您存在重度抑郁情绪，强烈建议立即寻求专业心理医生的帮助。");
        }
        return content.toString();
    }

    private String generateInterpersonalReport(ExamRecord exam) {
        StringBuilder content = new StringBuilder();
        content.append("【人际关系综合诊断报告】\n\n");
        content.append("测评时间：").append(exam.getCreateTime()).append("\n");
        content.append("总分：").append(exam.getTotalScore()).append("\n");
        content.append("等级：").append(exam.getLevel()).append("\n");
        content.append("风险评估：").append(exam.getRisk()).append("\n\n");
        content.append("【建议】\n");
        if (exam.getLevel().contains("良好")) {
            content.append("您的人际关系良好，请继续保持积极的社交态度。");
        } else if (exam.getLevel().contains("一般")) {
            content.append("您的人际关系一般，建议学习一些沟通技巧，主动参与社交活动。");
        } else if (exam.getLevel().contains("较差")) {
            content.append("您的人际关系较差，建议寻求心理咨询师的帮助，学习如何改善人际关系。");
        } else {
            content.append("您的人际关系存在严重困扰，强烈建议寻求专业心理咨询帮助。");
        }
        return content.toString();
    }

    private String generateEPQReport(ExamRecord exam) {
        StringBuilder content = new StringBuilder();
        content.append("【艾森克人格问卷(EPQ)报告】\n\n");
        content.append("测评时间：").append(exam.getCreateTime()).append("\n");
        content.append("总分：").append(exam.getTotalScore()).append("\n");
        content.append("等级：").append(exam.getLevel()).append("\n");
        content.append("风险评估：").append(exam.getRisk()).append("\n\n");
        content.append("【建议】\n");
        if (exam.getLevel().contains("正常")) {
            content.append("您的人格特征正常，请继续保持良好的自我认知。");
        } else if (exam.getLevel().contains("轻微偏离")) {
            content.append("您的人格特征存在轻微偏离，建议关注自我成长，保持积极心态。");
        } else if (exam.getLevel().contains("中度偏离")) {
            content.append("您的人格特征存在中度偏离，建议寻求心理咨询师的帮助，了解自己的人格特点。");
        } else {
            content.append("您的人格特征存在显著偏离，强烈建议寻求专业心理评估和咨询。");
        }
        return content.toString();
    }

    private String generateStressCopingReport(ExamRecord exam) {
        StringBuilder content = new StringBuilder();
        content.append("【压力应对方式问卷报告】\n\n");
        content.append("测评时间：").append(exam.getCreateTime()).append("\n");
        content.append("总分：").append(exam.getTotalScore()).append("\n");
        content.append("等级：").append(exam.getLevel()).append("\n");
        content.append("风险评估：").append(exam.getRisk()).append("\n\n");
        content.append("【建议】\n");
        if (exam.getLevel().contains("积极")) {
            content.append("您的应对方式积极有效，请继续保持良好的压力管理能力。");
        } else if (exam.getLevel().contains("一般")) {
            content.append("您的应对方式一般，建议学习更多有效的压力管理技巧，如时间管理、放松训练等。");
        } else if (exam.getLevel().contains("消极")) {
            content.append("您的应对方式较为消极，建议寻求心理咨询师的帮助，学习积极的应对策略。");
        } else {
            content.append("您的应对方式不良，强烈建议寻求专业心理指导，学习健康的压力应对方法。");
        }
        return content.toString();
    }
}