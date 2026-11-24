package com.mental.controller;

import com.mental.common.Result;
import com.mental.entity.Resource;
import com.mental.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@CrossOrigin
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping("/list")
    public Result<List<Resource>> list() {
        List<Resource> list = resourceService.list();
        return Result.success(list);
    }

    // 获取播客列表
    @GetMapping("/podcast/list")
    public Result<List<Resource>> getPodcastList() {
        List<Resource> list = resourceService.listByType("podcast");
        return Result.success(list);
    }

    // 获取单个播客详情
    @GetMapping("/podcast/{id}")
    public Result<Resource> getPodcastById(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource != null && "podcast".equals(resource.getType())) {
            return Result.success(resource);
        }
        return Result.error("播客不存在");
    }
}