package com.mental.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mental.entity.Emergency;
import com.mental.mapper.EmergencyMapper;
import com.mental.service.EmergencyService;
import org.springframework.stereotype.Service;

@Service
public class EmergencyServiceImpl extends ServiceImpl<EmergencyMapper, Emergency> implements EmergencyService {

}