package com.mental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mental.entity.ExamRecord;
import com.mental.vo.ExamRecordVO;

import java.util.List;

public interface ExamRecordService extends IService<ExamRecord> {

    /**
     * 获取用户最新的测评记录
     * @param userId 用户ID
     * @param limit 获取数量
     * @return 测评记录列表
     */
    List<ExamRecordVO> getLatestExamRecords(Long userId, int limit);
}