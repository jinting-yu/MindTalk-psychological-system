package com.mental.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("consult")
public class Consult {
    private Long id;
    private Long userId;
    private Long counselorId;
    private Date appointTime;
    private String status;
    private Date createTime;
}