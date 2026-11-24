package com.mental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mental.entity.ExamRecord;
import com.mental.entity.Scale;
import com.mental.mapper.ExamRecordMapper;
import com.mental.mapper.ScaleMapper;
import com.mental.service.ExamRecordService;
import com.mental.vo.ExamRecordVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {

    @Resource
    private ScaleMapper scaleMapper;

    @Override
    public List<ExamRecordVO> getLatestExamRecords(Long userId, int limit) {
        if (userId == null) {
            return new ArrayList<>();
        }

        List<ExamRecord> records = baseMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getCreateTime)
                .last("LIMIT " + limit));

        return records.stream().map(record -> {
            Scale scale = scaleMapper.selectById(record.getScaleId());

            ExamRecordVO vo = new ExamRecordVO();
            vo.setId(record.getId());
            vo.setScaleId(record.getScaleId());
            vo.setScaleName(scale != null ? scale.getName() : "未知测评");
            vo.setScaleType(getScaleType(scale != null ? scale.getName() : ""));
            vo.setScore(record.getTotalScore());
            vo.setLevel(record.getLevel());
            vo.setSuggestion(record.getRisk());
            vo.setCreateTime(record.getCreateTime());

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据量表名称获取量表类型
     */
    private String getScaleType(String scaleName) {
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
}