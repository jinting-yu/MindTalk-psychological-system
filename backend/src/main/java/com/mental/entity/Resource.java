package com.mental.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("resource")
public class Resource {
    private Long id;
    private String title;
    private String content;
    private String type; // 可以是 "article" 或 "podcast"
    private String audioUrl; // 播客音频文件路径
    private Integer duration; // 播客时长（秒）
    private Date createTime;
}
