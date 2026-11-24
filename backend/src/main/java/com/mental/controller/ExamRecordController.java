package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.common.Result;
import com.mental.entity.ExamRecord;
import com.mental.security.UserContext;
import com.mental.service.ExamRecordService;
import com.mental.util.MentalLevelUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin
public class ExamRecordController {

    @Resource
    private ExamRecordService examRecordService;

    @PostMapping("/submit")
    public Result submit(@RequestBody ExamRecord record) {
        // 设置用户ID
        record.setUserId(UserContext.getUserId());

        // 根据 scaleId 选择对应的评分方法
        String[] lr = getLevelAndRiskByScaleId(record.getScaleId(), record.getTotalScore());
        record.setLevel(lr[0]);
        record.setRisk(lr[1]);

        // 保存记录
        examRecordService.save(record);

        // 返回保存的记录（包含自动生成的ID）
        return Result.success(record);
    }

    @GetMapping("/record")
    public Result record() {
        List<ExamRecord> list = examRecordService.list(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, UserContext.getUserId())
                        .orderByDesc(ExamRecord::getCreateTime)
        );
        return Result.success(list);
    }

    /**
     * 根据量表ID选择对应的评分方法
     * @param scaleId 量表ID
     * @param totalScore 原始总分
     * @return [等级, 风险建议]
     */
    private String[] getLevelAndRiskByScaleId(Long scaleId, int totalScore) {
        // scaleId 对应关系（根据你的数据库 scale 表）
        // 1: 心理健康检测（通用）
        // 2: SAS焦虑自评量表
        // 3: SDS抑郁自评量表
        // 4: 人际关系综合诊断
        // 5: EPQ艾森克人格问卷
        // 6: 压力应对方式问卷

        if (scaleId == 2) {
            // SAS焦虑自评量表
            return MentalLevelUtil.getSASLevelAndRisk(totalScore);
        } else if (scaleId == 3) {
            // SDS抑郁自评量表
            return MentalLevelUtil.getSDSLevelAndRisk(totalScore);
        } else if (scaleId == 4) {
            // 人际关系综合诊断
            return MentalLevelUtil.getInterpersonalLevelAndRisk(totalScore);
        } else if (scaleId == 5) {
            // 艾森克人格问卷(EPQ)
            return MentalLevelUtil.getEPQLevelAndRisk(totalScore);
        } else if (scaleId == 6) {
            // 压力应对方式问卷
            return MentalLevelUtil.getStressCopingLevelAndRisk(totalScore);
        } else {
            // 默认通用评分
            return MentalLevelUtil.getLevelAndRisk(totalScore);
        }
    }
}