package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mental.common.Result;
import com.mental.entity.ExamRecord;
import com.mental.entity.Question;
import com.mental.entity.Scale;
import com.mental.security.UserContext;
import com.mental.service.ExamRecordService;
import com.mental.service.QuestionService;
import com.mental.service.ScaleService;
import com.mental.util.MentalLevelUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scale")
@CrossOrigin
public class ScaleController {

    @Resource
    private ScaleService scaleService;

    @Resource
    private QuestionService questionService;

    @Resource
    private ExamRecordService examRecordService;

    @GetMapping("/list")
    public Result list() {
        return Result.success(scaleService.list(new LambdaQueryWrapper<Scale>().eq(Scale::getStatus, 1)));
    }

    @GetMapping("/question/{scaleId}")
    public Result question(@PathVariable Long scaleId) {
        List<Question> list = questionService.list(new LambdaQueryWrapper<Question>().eq(Question::getScaleId, scaleId));
        return Result.success(list);
    }

    @PostMapping("/submit")
    public Result submitAnswer(@RequestBody Map<String, Object> submitData) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }

        Long scaleId = Long.valueOf(submitData.get("scaleId").toString());
        List<Integer> choices = (List<Integer>) submitData.get("choices");

        List<Question> questions = questionService.list(new LambdaQueryWrapper<Question>().eq(Question::getScaleId, scaleId));

        int totalScore = 0;
        for (int i = 0; i < questions.size() && i < choices.size(); i++) {
            Question q = questions.get(i);
            int answerScore = choices.get(i);
            if (q.getScore() == 1) {
                answerScore = 5 - answerScore;
            }
            totalScore += answerScore;
        }

        Scale scale = scaleService.getById(scaleId);
        String scaleName = scale != null ? scale.getName() : "";

        String[] levelAndRisk;
        if (scaleName.contains("SAS") || scaleName.contains("焦虑")) {
            levelAndRisk = MentalLevelUtil.getSASLevelAndRisk(totalScore);
        } else if (scaleName.contains("SDS") || scaleName.contains("抑郁")) {
            levelAndRisk = MentalLevelUtil.getSDSLevelAndRisk(totalScore);
        } else if (scaleName.contains("人际")) {
            levelAndRisk = MentalLevelUtil.getInterpersonalLevelAndRisk(totalScore);
        } else if (scaleName.contains("EPQ") || scaleName.contains("人格")) {
            levelAndRisk = MentalLevelUtil.getEPQLevelAndRisk(totalScore);
        } else if (scaleName.contains("压力")) {
            levelAndRisk = MentalLevelUtil.getStressCopingLevelAndRisk(totalScore);
        } else {
            levelAndRisk = MentalLevelUtil.getLevelAndRisk(totalScore);
        }

        ExamRecord record = new ExamRecord();
        record.setUserId(currentUserId);
        record.setScaleId(scaleId);
        record.setTotalScore(totalScore);
        record.setLevel(levelAndRisk[0]);
        record.setRisk(levelAndRisk[1]);
        record.setCreateTime(new Date());

        examRecordService.save(record);

        Map<String, Object> result = new HashMap<>();
        result.put("resultId", record.getId());
        result.put("resultName", levelAndRisk[0]);
        result.put("resultDesc", levelAndRisk[1]);
        result.put("totalScore", totalScore);
        result.put("level", levelAndRisk[0]);
        result.put("createTime", record.getCreateTime());

        return Result.success(result);
    }

    @GetMapping("/my/latest")
    public Result getLatestRecord() {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }

        ExamRecord latestRecord = examRecordService.getOne(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, currentUserId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("LIMIT 1"));

        List<ExamRecord> allRecords = examRecordService.list(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, currentUserId));

        int totalCount = allRecords.size();
        double avgScore = allRecords.stream()
                .mapToInt(ExamRecord::getTotalScore)
                .average()
                .orElse(0.0);

        if (latestRecord == null) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("hasRecord", false);
            emptyResult.put("totalCount", 0);
            emptyResult.put("avgScore", 0);
            return Result.success(emptyResult);
        }

        Scale scale = scaleService.getById(latestRecord.getScaleId());
        String scaleName = scale != null ? scale.getName() : "未知测评";

        Map<String, Object> result = new HashMap<>();
        result.put("hasRecord", true);
        result.put("totalCount", totalCount);
        result.put("avgScore", Math.round(avgScore));
        result.put("latestDate", formatLatestDate(latestRecord.getCreateTime()));

        Map<String, Object> latest = new HashMap<>();
        latest.put("id", latestRecord.getId());
        latest.put("scaleName", scaleName);
        latest.put("totalScore", latestRecord.getTotalScore());
        latest.put("level", latestRecord.getLevel());
        latest.put("risk", latestRecord.getRisk());
        latest.put("createTime", latestRecord.getCreateTime());
        result.put("latest", latest);

        return Result.success(result);
    }

    @GetMapping("/my/records")
    public Result getMyRecords(@RequestParam(defaultValue = "1") int current,
                               @RequestParam(defaultValue = "10") int pageSize,
                               @RequestParam(required = false) String scaleName,
                               @RequestParam(required = false) String level) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }

        Page<ExamRecord> page = new Page<>(current, pageSize);

        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, currentUserId);

        if (level != null && !level.isEmpty()) {
            wrapper.like(ExamRecord::getLevel, level);
        }

        wrapper.orderByDesc(ExamRecord::getCreateTime);

        Page<ExamRecord> recordPage = examRecordService.page(page, wrapper);

        List<Map<String, Object>> records = recordPage.getRecords().stream().map(record -> {
            Scale scale = scaleService.getById(record.getScaleId());
            String scaleNameVal = scale != null ? scale.getName() : "未知测评";

            if (scaleName != null && !scaleName.isEmpty() && !scaleNameVal.contains(scaleName)) {
                return null;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("resultId", record.getId());
            item.put("scaleId", record.getScaleId());
            item.put("scaleName", scaleNameVal);
            item.put("scaleType", getScaleType(scaleNameVal));
            item.put("totalScore", record.getTotalScore());
            item.put("score", record.getTotalScore());
            item.put("level", record.getLevel());
            item.put("resultName", getResultNameByLevel(record.getLevel()));
            item.put("risk", record.getRisk());
            item.put("resultDesc", getResultDescByLevel(record.getLevel()));
            item.put("suggestion", record.getRisk());
            item.put("createTime", record.getCreateTime());
            item.put("updateTime", record.getCreateTime());
            return item;
        }).filter(item -> item != null).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", recordPage.getTotal());
        result.put("current", recordPage.getCurrent());
        result.put("pageSize", recordPage.getSize());

        return Result.success(result);
    }

    @DeleteMapping("/my/record/{id}")
    public Result deleteRecord(@PathVariable Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }

        ExamRecord record = examRecordService.getById(id);
        if (record == null || !record.getUserId().equals(currentUserId)) {
            return Result.error("记录不存在或无权限删除");
        }

        examRecordService.removeById(id);
        return Result.success("删除成功");
    }

    private String formatLatestDate(Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd");
        return sdf.format(date);
    }

    private String getScaleType(String scaleName) {
        if (scaleName == null) return "心理测评";
        if (scaleName.contains("SAS") || scaleName.contains("焦虑")) {
            return "焦虑测评";
        } else if (scaleName.contains("SDS") || scaleName.contains("抑郁")) {
            return "抑郁测评";
        } else if (scaleName.contains("人际")) {
            return "人际关系测评";
        } else if (scaleName.contains("EPQ") || scaleName.contains("人格")) {
            return "人格测评";
        } else if (scaleName.contains("压力")) {
            return "压力测评";
        } else if (scaleName.contains("MBTI") || scaleName.contains("性格")) {
            return "性格测评";
        } else {
            return "心理测评";
        }
    }

    private String getResultNameByLevel(String level) {
        if (level == null) return "测评结果";
        switch (level) {
            case "正常": return "心理健康状况良好";
            case "轻度": return "轻度心理困扰";
            case "中度": return "中度心理困扰";
            case "重度": return "重度心理困扰";
            case "重度抑郁": return "重度抑郁";
            case "中度抑郁": return "中度抑郁";
            case "轻度抑郁": return "轻度抑郁";
            case "重度焦虑": return "重度焦虑";
            case "中度焦虑": return "中度焦虑";
            case "轻度焦虑": return "轻度焦虑";
            default: return level;
        }
    }

    private String getResultDescByLevel(String level) {
        if (level == null) return "请关注您的心理健康。";
        switch (level) {
            case "正常": return "您的心理健康状况良好，请继续保持。";
            case "轻度": return "您存在轻度心理困扰，建议进行适当的放松和调节。";
            case "中度": return "您存在中度心理困扰，建议寻求专业心理咨询。";
            case "重度": return "您存在重度心理困扰，强烈建议立即寻求专业帮助。";
            case "重度抑郁": return "您存在重度抑郁情绪，强烈建议立即寻求专业帮助。";
            case "中度抑郁": return "您存在中度抑郁情绪，建议寻求心理咨询。";
            case "轻度抑郁": return "您存在轻度抑郁情绪，建议多参加户外活动。";
            case "重度焦虑": return "您存在重度焦虑情绪，强烈建议立即寻求专业帮助。";
            case "中度焦虑": return "您存在中度焦虑情绪，建议寻求心理咨询。";
            case "轻度焦虑": return "您存在轻度焦虑情绪，建议学习放松技巧。";
            default: return "请关注您的心理健康。";
        }
    }
}