package com.mental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mental.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测评记录 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，自动提供 CRUD 方法
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

}