package com.mental.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("exam_record")
public class ExamRecord {
    private Long id;
    private Long userId;
    private Long scaleId;
    private Integer totalScore;
    private String level;
    private String risk;
    private Date createTime;
}