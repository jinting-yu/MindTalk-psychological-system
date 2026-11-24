package com.mental.controller;

import com.mental.common.Result;
import com.mental.entity.Scale;
import com.mental.service.ScaleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/app")
@CrossOrigin
public class AppController {

    @Resource
    private ScaleService scaleService;

    /**
     * 根据ID获取应用详情
     */
    @GetMapping("/get")
    public Result getById(@RequestParam Long id) {
        Scale scale = scaleService.getById(id);
        if (scale == null) {
            return Result.error("应用不存在");
        }
        return Result.success(scale);
    }

    /**
     * 获取应用详情VO
     */
    @GetMapping("/get/vo")
    public Result getVoById(@RequestParam Long id) {
        Scale scale = scaleService.getById(id);
        if (scale == null) {
            return Result.error("应用不存在");
        }
        return Result.success(scale);
    }
}