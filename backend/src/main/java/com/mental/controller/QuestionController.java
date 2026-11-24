package com.mental.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mental.common.Result;
import com.mental.entity.Question;
import com.mental.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/api/question")
@CrossOrigin
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @PostMapping("/list/page/vo")
    public Result listPageVo(@RequestBody Map<String, Object> params) {
        Long appId = Long.parseLong(params.get("appId").toString());

        // 获取所有题目（不分页）
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getScaleId, appId);
        wrapper.orderByAsc(Question::getId);

        List<Question> questionList = questionService.list(wrapper);

        System.out.println("找到题目数量: " + questionList.size());

        // 构建 questionContent 数组
        List<Map<String, Object>> questionContent = new ArrayList<>();

        for (Question question : questionList) {
            Map<String, Object> content = new HashMap<>();
            content.put("title", question.getContent());

            // 生成选项
            List<Map<String, String>> options = new ArrayList<>();
            options.add(createOption("1", "非常不符合"));
            options.add(createOption("2", "不太符合"));
            options.add(createOption("3", "一般"));
            options.add(createOption("4", "比较符合"));
            options.add(createOption("5", "非常符合"));
            content.put("options", options);

            questionContent.add(content);
        }

        System.out.println("返回 questionContent: " + questionContent);

        // 返回 questionContent
        return Result.success(questionContent);
    }

    private Map<String, String> createOption(String key, String value) {
        Map<String, String> option = new HashMap<>();
        option.put("key", key);
        option.put("value", value);
        return option;
    }
}