package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.common.Result;
import com.mental.entity.Consult;
import com.mental.security.Access;
import com.mental.security.UserContext;
import com.mental.service.ConsultService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/consult")
@CrossOrigin
public class ConsultController {

    @Resource
    private ConsultService consultService;

    @PostMapping("/appoint")
    public Result appoint(@RequestBody Consult consult) {
        consult.setUserId(UserContext.getUserId());
        consult.setStatus("WAIT");
        consultService.save(consult);
        return Result.success("预约成功");
    }

    @GetMapping("/my")
    public Result my() {
        List<Consult> list = consultService.list(
                new LambdaQueryWrapper<Consult>().eq(Consult::getUserId, UserContext.getUserId())
        );
        return Result.success(list);
    }

    @PostMapping("/confirm/{id}")
    public Result confirm(@PathVariable Long id) {
        Result r = Access.checkAdmin();
        if (r != null) return r;
        Consult consult = consultService.getById(id);
        consult.setStatus("SUCCESS");
        consultService.updateById(consult);
        return Result.success("已确认");
    }
}