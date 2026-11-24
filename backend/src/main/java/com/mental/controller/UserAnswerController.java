package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mental.common.Result;
import com.mental.entity.ExamRecord;
import com.mental.entity.Report;
import com.mental.security.UserContext;
import com.mental.service.ExamRecordService;
import com.mental.service.ReportService;
import com.mental.util.MentalLevelUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/userAnswer","/answer"})
@CrossOrigin
public class UserAnswerController {

    @Resource
    private ExamRecordService examRecordService;

    @Resource
    private ReportService reportService;

    @GetMapping("/generate/id")
    public Result generateId() {
        long id = System.currentTimeMillis();
        return Result.success(id);
    }

    @PostMapping("/add")
    public Result addUserAnswer(@RequestBody Map<String, Object> params) {
        try {
            Long appId = Long.parseLong(params.get("appId").toString());
            @SuppressWarnings("unchecked")
            List<String> choices = (List<String>) params.get("choices");

            int totalScore = 0;
            for (String choice : choices) {
                try {
                    totalScore += Integer.parseInt(choice);
                } catch (NumberFormatException e) {
                    totalScore += mapChoiceToScore(choice);
                }
            }

            String level, risk;
            if (appId == 2) {
                String[] lr = MentalLevelUtil.getSASLevelAndRisk(totalScore);
                level = lr[0];
                risk = lr[1];
            } else if (appId == 3) {
                String[] lr = MentalLevelUtil.getSDSLevelAndRisk(totalScore);
                level = lr[0];
                risk = lr[1];
            } else if (appId == 4) {
                String[] lr = MentalLevelUtil.getInterpersonalLevelAndRisk(totalScore);
                level = lr[0];
                risk = lr[1];
            } else if (appId == 5) {
                String[] lr = MentalLevelUtil.getEPQLevelAndRisk(totalScore);
                level = lr[0];
                risk = lr[1];
            } else if (appId == 6) {
                String[] lr = MentalLevelUtil.getStressCopingLevelAndRisk(totalScore);
                level = lr[0];
                risk = lr[1];
            } else {
                double avgScore = (double) totalScore / choices.size();
                // 修改：分数越高越健康
                String[] lr = getLevelAndRiskByHighScoreBetter(avgScore);
                level = lr[0];
                risk = lr[1];
            }

            ExamRecord record = new ExamRecord();
            record.setUserId(UserContext.getUserId() != null ? UserContext.getUserId() : 1L);
            record.setScaleId(appId);
            record.setTotalScore(totalScore);
            record.setLevel(level);
            record.setRisk(risk);
            record.setCreateTime(new Date());

            examRecordService.save(record);
            createReport(record);

            return Result.success(record.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交答案失败：" + e.getMessage());
        }
    }

    @GetMapping("/get/vo")
    public Result getUserAnswerVoById(@RequestParam Long id) {
        return getUserAnswerVo(id);
    }

    @GetMapping("/result/{id}")
    public Result getUserAnswerVoByPathId(@PathVariable Long id) {
        return getUserAnswerVo(id);
    }

    private Result getUserAnswerVo(Long id) {
        try {
            System.out.println("获取答题结果, id: " + id);
            ExamRecord record = examRecordService.getById(id);
            if (record == null) {
                return Result.error("记录不存在");
            }

            LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Report::getExamId, id);
            Report report = reportService.getOne(wrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("id", record.getId());
            result.put("resultId", record.getId());
            result.put("resultScore", record.getTotalScore());
            result.put("resultName", getResultNameByLevel(record.getLevel()));
            result.put("appId", record.getScaleId());
            result.put("appType", 0);
            result.put("scoringStrategy", 0);
            result.put("createTime", record.getCreateTime());
            result.put("choices", new ArrayList<>());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userName", "测试用户");
            userInfo.put("userAvatar", "");
            result.put("user", userInfo);

            if (report != null && report.getContent() != null) {
                result.put("resultDesc", report.getContent());
                result.put("reportContent", report.getContent());
            } else {
                result.put("resultDesc", getResultDescByLevel(record.getLevel()));
                result.put("reportContent", getResultDescByLevel(record.getLevel()));
            }

            return Result.success(result);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取结果失败: " + e.getMessage());
        }
    }

    @GetMapping("/my/page")
    public Result getMyRecordsPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String resultName,
            @RequestParam(required = false) String resultDesc,
            @RequestParam(required = false) Long appId) {
        try {
            Long userId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
            Page<ExamRecord> page = new Page<>(current, pageSize);

            LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamRecord::getUserId, userId);

            if (resultName != null && !resultName.isEmpty()) {
                wrapper.like(ExamRecord::getLevel, resultName);
            }
            if (appId != null) {
                wrapper.eq(ExamRecord::getScaleId, appId);
            }

            wrapper.orderByDesc(ExamRecord::getCreateTime);
            Page<ExamRecord> recordPage = examRecordService.page(page, wrapper);

            List<Map<String, Object>> records = new ArrayList<>();
            for (ExamRecord record : recordPage.getRecords()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", record.getId());
                item.put("resultId", record.getId());
                item.put("resultScore", record.getTotalScore());
                item.put("resultName", getResultNameByLevel(record.getLevel()));
                item.put("resultDesc", getResultDescByLevel(record.getLevel()));
                item.put("appId", record.getScaleId());
                item.put("appType", 0);
                item.put("scoringStrategy", 0);
                item.put("createTime", record.getCreateTime());
                item.put("updateTime", record.getCreateTime());
                item.put("choices", new ArrayList<>());
                records.add(item);
            }

            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("records", records);
            pageResult.put("total", recordPage.getTotal());
            pageResult.put("current", recordPage.getCurrent());
            pageResult.put("pageSize", recordPage.getSize());

            return Result.success(pageResult);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取测评记录失败: " + e.getMessage());
        }
    }

    @PostMapping("/my/list/page/vo")
    public Result getMyRecordsByPage(@RequestBody Map<String, Object> params) {
        try {
            int current = params.get("current") != null ? Integer.parseInt(params.get("current").toString()) : 1;
            int pageSize = params.get("pageSize") != null ? Integer.parseInt(params.get("pageSize").toString()) : 10;

            Long userId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;
            Page<ExamRecord> page = new Page<>(current, pageSize);

            LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamRecord::getUserId, userId)
                    .orderByDesc(ExamRecord::getCreateTime);

            Page<ExamRecord> recordPage = examRecordService.page(page, wrapper);

            List<Map<String, Object>> records = new ArrayList<>();
            for (ExamRecord record : recordPage.getRecords()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", record.getId());
                item.put("resultId", record.getId());
                item.put("resultScore", record.getTotalScore());
                item.put("resultName", getResultNameByLevel(record.getLevel()));
                item.put("resultDesc", getResultDescByLevel(record.getLevel()));
                item.put("appId", record.getScaleId());
                item.put("appType", 0);
                item.put("scoringStrategy", 0);
                item.put("createTime", record.getCreateTime());
                item.put("choices", new ArrayList<>());
                records.add(item);
            }

            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("records", records);
            pageResult.put("total", recordPage.getTotal());
            pageResult.put("current", recordPage.getCurrent());
            pageResult.put("pageSize", recordPage.getSize());

            return Result.success(pageResult);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取测评记录失败: " + e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Result deleteUserAnswer(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            examRecordService.removeById(id);

            LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Report::getExamId, id);
            reportService.remove(wrapper);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    private void createReport(ExamRecord record) {
        try {
            String content = generateReportContent(record);
            Report report = new Report();
            report.setUserId(record.getUserId());
            report.setExamId(record.getId());
            report.setContent(content);
            report.setCreateTime(new Date());
            reportService.save(report);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateReportContent(ExamRecord record) {
        if (record.getScaleId() == 2) {
            return generateSASReport(record);
        } else if (record.getScaleId() == 3) {
            return generateSDSReport(record);
        } else if (record.getScaleId() == 4) {
            return generateInterpersonalReport(record);
        } else if (record.getScaleId() == 5) {
            return generateEPQReport(record);
        } else if (record.getScaleId() == 6) {
            return generateStressCopingReport(record);
        } else {
            StringBuilder content = new StringBuilder();
            content.append("【心理健康测评报告】\n\n");
            content.append("测评时间：").append(record.getCreateTime()).append("\n");
            content.append("总分：").append(record.getTotalScore()).append("\n");
            content.append("等级：").append(record.getLevel()).append("\n");
            content.append("风险评估：").append(record.getRisk()).append("\n\n");
            content.append("【建议】\n");
            content.append(getSuggestionByLevel(record.getLevel()));
            return content.toString();
        }
    }

    private String generateSASReport(ExamRecord record) {
        StringBuilder content = new StringBuilder();
        content.append("【焦虑自评量表(SAS)报告】\n\n");
        content.append("测评时间：").append(record.getCreateTime()).append("\n");
        content.append("总分：").append(record.getTotalScore()).append("\n");
        content.append("标准分：").append(record.getTotalScore() * 1.25).append("\n");
        content.append("等级：").append(record.getLevel()).append("\n");
        content.append("风险评估：").append(record.getRisk()).append("\n\n");
        content.append("【建议】\n");
        if (record.getLevel().contains("正常")) {
            content.append("您的焦虑水平正常，请继续保持良好的心理状态。");
        } else if (record.getLevel().contains("轻度")) {
            content.append("您存在轻度焦虑情绪，建议学习放松技巧，如深呼吸、冥想等。");
        } else if (record.getLevel().contains("中度")) {
            content.append("您存在中度焦虑情绪，建议寻求心理咨询师的帮助。");
        } else {
            content.append("您存在重度焦虑情绪，强烈建议立即寻求专业心理医生的帮助。");
        }
        return content.toString();
    }

    private String generateSDSReport(ExamRecord record) {
        StringBuilder content = new StringBuilder();
        content.append("【抑郁自评量表(SDS)报告】\n\n");
        content.append("测评时间：").append(record.getCreateTime()).append("\n");
        content.append("总分：").append(record.getTotalScore()).append("\n");
        content.append("标准分：").append(record.getTotalScore() * 1.25).append("\n");
        content.append("等级：").append(record.getLevel()).append("\n");
        content.append("风险评估：").append(record.getRisk()).append("\n\n");
        content.append("【建议】\n");
        if (record.getLevel().contains("正常")) {
            content.append("您的情绪状态良好，请继续保持积极的生活态度。");
        } else if (record.getLevel().contains("轻度")) {
            content.append("您存在轻度抑郁情绪，建议多参加户外活动，与朋友交流，保持规律作息。");
        } else if (record.getLevel().contains("中度")) {
            content.append("您存在中度抑郁情绪，建议寻求心理咨询师的帮助，必要时就医。");
        } else {
            content.append("您存在重度抑郁情绪，强烈建议立即寻求专业心理医生的帮助。");
        }
        return content.toString();
    }

    private String generateInterpersonalReport(ExamRecord record) {
        StringBuilder content = new StringBuilder();
        content.append("【人际关系综合诊断报告】\n\n");
        content.append("测评时间：").append(record.getCreateTime()).append("\n");
        content.append("总分：").append(record.getTotalScore()).append("\n");
        content.append("等级：").append(record.getLevel()).append("\n");
        content.append("风险评估：").append(record.getRisk()).append("\n\n");
        content.append("【建议】\n");
        if (record.getLevel().contains("良好")) {
            content.append("您的人际关系良好，请继续保持积极的社交态度。");
        } else if (record.getLevel().contains("一般")) {
            content.append("您的人际关系一般，建议学习一些沟通技巧，主动参与社交活动。");
        } else if (record.getLevel().contains("较差")) {
            content.append("您的人际关系较差，建议寻求心理咨询师的帮助，学习如何改善人际关系。");
        } else {
            content.append("您的人际关系存在严重困扰，强烈建议寻求专业心理咨询帮助。");
        }
        return content.toString();
    }

    private String generateEPQReport(ExamRecord record) {
        StringBuilder content = new StringBuilder();
        content.append("【艾森克人格问卷(EPQ)报告】\n\n");
        content.append("测评时间：").append(record.getCreateTime()).append("\n");
        content.append("总分：").append(record.getTotalScore()).append("\n");
        content.append("等级：").append(record.getLevel()).append("\n");
        content.append("风险评估：").append(record.getRisk()).append("\n\n");
        content.append("【建议】\n");
        if (record.getLevel().contains("正常")) {
            content.append("您的人格特征正常，请继续保持良好的自我认知。");
        } else if (record.getLevel().contains("轻微偏离")) {
            content.append("您的人格特征存在轻微偏离，建议关注自我成长，保持积极心态。");
        } else if (record.getLevel().contains("中度偏离")) {
            content.append("您的人格特征存在中度偏离，建议寻求心理咨询师的帮助，了解自己的人格特点。");
        } else {
            content.append("您的人格特征存在显著偏离，强烈建议寻求专业心理评估和咨询。");
        }
        return content.toString();
    }

    private String generateStressCopingReport(ExamRecord record) {
        StringBuilder content = new StringBuilder();
        content.append("【压力应对方式问卷报告】\n\n");
        content.append("测评时间：").append(record.getCreateTime()).append("\n");
        content.append("总分：").append(record.getTotalScore()).append("\n");
        content.append("等级：").append(record.getLevel()).append("\n");
        content.append("风险评估：").append(record.getRisk()).append("\n\n");
        content.append("【建议】\n");
        if (record.getLevel().contains("积极")) {
            content.append("您的应对方式积极有效，请继续保持良好的压力管理能力。");
        } else if (record.getLevel().contains("一般")) {
            content.append("您的应对方式一般，建议学习更多有效的压力管理技巧，如时间管理、放松训练等。");
        } else if (record.getLevel().contains("消极")) {
            content.append("您的应对方式较为消极，建议寻求心理咨询师的帮助，学习积极的应对策略。");
        } else {
            content.append("您的应对方式不良，强烈建议寻求专业心理指导，学习健康的压力应对方法。");
        }
        return content.toString();
    }

    /**
     * 新增方法：分数越高越健康的等级和风险评估
     * @param avgScore 平均分 (总分/题目数)
     * @return [等级, 风险评估]
     */
    private String[] getLevelAndRiskByHighScoreBetter(double avgScore) {
        String level;
        String risk;

        // 分数越高越健康：高分对应正常，低分对应重度
        // 假设满分5分，5分最高分表示最健康
        if (avgScore >= 4.0) {
            level = "正常";
            risk = "心理健康状况良好";
        } else if (avgScore >= 3.0) {
            level = "轻度";
            risk = "存在轻度心理困扰，建议关注";
        } else if (avgScore >= 2.0) {
            level = "中度";
            risk = "存在中度心理困扰，建议咨询";
        } else {
            level = "重度";
            risk = "存在重度心理困扰，强烈建议求助";
        }

        return new String[]{level, risk};
    }

    private String getResultNameByLevel(String level) {
        switch (level) {
            case "正常": return "心理健康状况良好";
            case "轻度": return "轻度心理困扰";
            case "中度": return "中度心理困扰";
            case "重度": return "重度心理困扰";
            case "心理健康": return "心理健康状况良好";
            case "基本健康": return "心理健康状况良好";
            case "轻度心理风险": return "轻度心理风险";
            case "中度心理风险": return "中度心理风险";
            case "重度心理风险": return "重度心理风险";
            case "轻度焦虑": return "轻度焦虑";
            case "中度焦虑": return "中度焦虑";
            case "重度焦虑": return "重度焦虑";
            case "轻度抑郁": return "轻度抑郁";
            case "中度抑郁": return "中度抑郁";
            case "重度抑郁": return "重度抑郁";
            case "人际关系良好": return "人际关系良好";
            case "人际关系一般": return "人际关系一般";
            case "人际关系较差": return "人际关系较差";
            case "人际关系严重困扰": return "人际关系严重困扰";
            case "人格特征正常": return "人格特征正常";
            case "人格特征轻微偏离": return "人格特征轻微偏离";
            case "人格特征中度偏离": return "人格特征中度偏离";
            case "人格特征显著偏离": return "人格特征显著偏离";
            case "应对方式积极": return "应对方式积极";
            case "应对方式一般": return "应对方式一般";
            case "应对方式消极": return "应对方式消极";
            case "应对方式不良": return "应对方式不良";
            default: return "心理健康测评结果";
        }
    }

    private String getResultDescByLevel(String level) {
        switch (level) {
            case "正常": return "您的心理健康状况良好，请继续保持。";
            case "轻度": return "您存在轻度心理困扰，建议进行适当的放松和调节。";
            case "中度": return "您存在中度心理困扰，建议寻求专业心理咨询。";
            case "重度": return "您存在重度心理困扰，强烈建议立即寻求专业帮助。";
            case "心理健康": return "您的心理健康状况良好，请继续保持。";
            case "基本健康": return "您的心理健康状况基本良好，建议注意自我调节。";
            case "轻度心理风险": return "您存在轻度心理风险，建议关注情绪变化，适当放松。";
            case "中度心理风险": return "您存在中度心理风险，建议预约心理咨询。";
            case "重度心理风险": return "您存在重度心理风险，强烈建议立即寻求专业帮助。";
            case "轻度焦虑": return "您存在轻度焦虑情绪，建议学习放松技巧。";
            case "中度焦虑": return "您存在中度焦虑情绪，建议寻求心理咨询。";
            case "重度焦虑": return "您存在重度焦虑情绪，强烈建议立即寻求专业帮助。";
            case "轻度抑郁": return "您存在轻度抑郁情绪，建议多参加户外活动。";
            case "中度抑郁": return "您存在中度抑郁情绪，建议寻求心理咨询。";
            case "重度抑郁": return "您存在重度抑郁情绪，强烈建议立即寻求专业帮助。";
            case "人际关系良好": return "您的人际关系良好，请继续保持。";
            case "人际关系一般": return "您的人际关系一般，建议学习沟通技巧。";
            case "人际关系较差": return "您的人际关系较差，建议寻求专业帮助。";
            case "人际关系严重困扰": return "您的人际关系存在严重困扰，强烈建议寻求专业帮助。";
            case "人格特征正常": return "您的人格特征正常，请继续保持良好的自我认知。";
            case "人格特征轻微偏离": return "您的人格特征存在轻微偏离，建议关注自我成长。";
            case "人格特征中度偏离": return "您的人格特征存在中度偏离，建议寻求专业评估。";
            case "人格特征显著偏离": return "您的人格特征存在显著偏离，强烈建议寻求专业帮助。";
            case "应对方式积极": return "您的应对方式积极有效，请继续保持。";
            case "应对方式一般": return "您的应对方式一般，建议学习更多压力管理技巧。";
            case "应对方式消极": return "您的应对方式较为消极，建议寻求专业指导。";
            case "应对方式不良": return "您的应对方式不良，强烈建议寻求专业帮助。";
            default: return "请关注您的心理健康。";
        }
    }

    private String getSuggestionByLevel(String level) {
        switch (level) {
            case "正常": return "保持规律作息，坚持运动，保持积极心态。";
            case "轻度": return "尝试深呼吸、冥想等放松技巧，适当运动，保持社交。";
            case "中度": return "建议预约学校心理咨询中心或专业心理咨询师进行咨询。";
            case "重度": return "请立即联系心理医生或拨打心理援助热线寻求帮助。";
            case "心理健康": return "保持规律作息，坚持运动，保持积极心态。";
            case "基本健康": return "保持规律作息，适当运动，保持积极心态。";
            case "轻度心理风险": return "建议关注情绪变化，适当放松，保持社交。";
            case "中度心理风险": return "建议预约学校心理咨询中心或专业心理咨询师进行咨询。";
            case "重度心理风险": return "请立即联系心理医生或拨打心理援助热线寻求帮助。";
            case "轻度焦虑": return "学习放松技巧，如深呼吸、冥想等，适当运动。";
            case "中度焦虑": return "建议预约心理咨询师进行专业指导。";
            case "重度焦虑": return "请立即联系心理医生或拨打心理援助热线寻求帮助。";
            case "轻度抑郁": return "多参加户外活动，与朋友交流，保持规律作息。";
            case "中度抑郁": return "建议预约心理咨询师进行专业指导，必要时就医。";
            case "重度抑郁": return "请立即联系心理医生或拨打心理援助热线寻求帮助。";
            case "人际关系良好": return "继续保持积极的社交态度，主动关心他人。";
            case "人际关系一般": return "学习沟通技巧，主动参与社交活动，培养同理心。";
            case "人际关系较差": return "建议寻求心理咨询师的帮助，学习如何改善人际关系。";
            case "人际关系严重困扰": return "强烈建议寻求专业心理咨询帮助，系统学习人际关系技巧。";
            case "人格特征正常": return "保持良好的自我认知，继续发展积极的人格特质。";
            case "人格特征轻微偏离": return "关注自我成长，通过阅读、学习等方式完善人格。";
            case "人格特征中度偏离": return "建议寻求心理咨询师的帮助，深入了解自己的人格特点。";
            case "人格特征显著偏离": return "强烈建议寻求专业心理评估和长期心理咨询。";
            case "应对方式积极": return "继续保持积极的应对策略，分享经验帮助他人。";
            case "应对方式一般": return "学习时间管理、放松训练等压力管理技巧。";
            case "应对方式消极": return "建议寻求心理咨询师的帮助，学习积极的应对策略。";
            case "应对方式不良": return "强烈建议寻求专业心理指导，建立健康的压力应对机制。";
            default: return "关注心理健康，必要时寻求帮助。";
        }
    }

    private int mapChoiceToScore(String choice) {
        switch (choice) {
            case "1": case "非常不符合": return 1;
            case "2": case "不太符合": return 2;
            case "3": case "一般": return 3;
            case "4": case "比较符合": return 4;
            case "5": case "非常符合": return 5;
            default: return 3;
        }
    }

    // 注意：这个方法已不再使用，但保留以兼容其他可能调用
    private String getLevelByAvgScore(double avgScore) {
        if (avgScore <= 1.5) return "正常";
        if (avgScore <= 2.5) return "轻度";
        if (avgScore <= 3.5) return "中度";
        return "重度";
    }

    // 注意：这个方法已不再使用，但保留以兼容其他可能调用
    private String getRiskByLevel(String level) {
        switch (level) {
            case "正常": return "心理健康状况良好";
            case "轻度": return "存在轻度心理困扰，建议关注";
            case "中度": return "存在中度心理困扰，建议咨询";
            case "重度": return "存在重度心理困扰，强烈建议求助";
            case "心理健康": return "无风险";
            case "基本健康": return "轻微压力，建议自我调节";
            case "轻度心理风险": return "建议关注情绪，适当放松";
            case "中度心理风险": return "建议预约心理咨询";
            case "重度心理风险": return "请立即联系辅导员或心理咨询师";
            case "轻度焦虑": return "建议自我调节";
            case "中度焦虑": return "建议心理咨询";
            case "重度焦虑": return "请立即寻求专业帮助";
            case "轻度抑郁": return "建议自我调节";
            case "中度抑郁": return "建议心理咨询";
            case "重度抑郁": return "请立即寻求专业帮助";
            case "人际关系良好": return "无困扰";
            case "人际关系一般": return "轻微困扰";
            case "人际关系较差": return "中度困扰";
            case "人际关系严重困扰": return "建议专业咨询";
            case "人格特征正常": return "无风险";
            case "人格特征轻微偏离": return "建议关注";
            case "人格特征中度偏离": return "建议咨询";
            case "人格特征显著偏离": return "建议专业评估";
            case "应对方式积极": return "无风险";
            case "应对方式一般": return "建议调整";
            case "应对方式消极": return "建议学习";
            case "应对方式不良": return "建议专业指导";
            default: return "请关注心理健康";
        }
    }
}