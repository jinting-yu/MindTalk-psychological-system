package com.mental.controller;

import com.mental.common.Result;
import com.mental.entity.Emergency;
import com.mental.security.UserContext;
import com.mental.service.EmergencyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/emergency")
@CrossOrigin
public class EmergencyController {

    @Resource
    private EmergencyService emergencyService;

    @PostMapping("/call")
    public Result call(@RequestBody Emergency emergency) {
        emergency.setUserId(UserContext.getUserId());
        emergencyService.save(emergency);
        return Result.success("求助已发送，相关人员将尽快联系你");
    }
}