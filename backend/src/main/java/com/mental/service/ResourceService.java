package com.mental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mental.entity.Resource;

import java.util.List;

public interface ResourceService extends IService<Resource> {
    List<Resource> listByType(String type);
}