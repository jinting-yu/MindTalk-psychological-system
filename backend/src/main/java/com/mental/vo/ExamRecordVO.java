package com.mental.vo;

import lombok.Data;
import java.util.Date;

@Data
public class ExamRecordVO {
    private Long id;
    private Long scaleId;
    private String scaleName;
    private String scaleType;
    private Integer score;
    private String level;
    private String suggestion;
    private Date createTime;
}