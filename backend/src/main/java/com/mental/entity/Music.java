package com.mental.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("music")
public class Music {
    private Long id;
    private String name;
    private String url;
    private Integer status;
}