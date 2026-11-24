package com.mental.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("report")
public class Report {
    private Long id;
    private Long userId;
    private Long examId;
    private String content;
    private Date createTime;
}