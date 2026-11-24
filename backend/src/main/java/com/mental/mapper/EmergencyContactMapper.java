package com.mental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mental.entity.EmergencyContact;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmergencyContactMapper extends BaseMapper<EmergencyContact> {

 //   @Select("SELECT * FROM emergency_contact WHERE user_id = #{userId} ORDER BY is_primary DESC")
    List<EmergencyContact> findByUserId(Long userId);

   // @Select("SELECT * FROM emergency_contact WHERE user_id = #{userId} AND is_primary = 1")
    EmergencyContact findPrimaryByUserId(Long userId);
}