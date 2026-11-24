package com.mental.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("question")
public class Question {
    private Long id;
    private Long scaleId;
    private String content;
    private Integer score;
}