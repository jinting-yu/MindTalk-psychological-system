package com.mental.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scale")
public class Scale {
    private Long id;
    private String name;
    private String description;
    private Integer status;
}