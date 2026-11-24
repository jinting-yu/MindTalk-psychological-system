package com.mental.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mental.entity.Consult;
import com.mental.mapper.ConsultMapper;
import com.mental.service.ConsultService;
import org.springframework.stereotype.Service;

@Service
public class ConsultServiceImpl extends ServiceImpl<ConsultMapper, Consult> implements ConsultService {

}