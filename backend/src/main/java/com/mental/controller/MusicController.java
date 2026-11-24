package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.common.Result;
import com.mental.entity.Music;
import com.mental.service.MusicService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/music")
@CrossOrigin
public class MusicController {

    @Resource
    private MusicService musicService;

    @GetMapping("/list")
    public Result list() {
        List<Music> list = musicService.list(new LambdaQueryWrapper<Music>().eq(Music::getStatus, 1));
        return Result.success(list);
    }
}