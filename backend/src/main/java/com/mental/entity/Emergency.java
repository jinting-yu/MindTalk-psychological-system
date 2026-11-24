package com.mental.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("emergency")
public class Emergency {
    private Long id;
    private Long userId;
    private String contactType;
    private String content;
    private Date createTime;
}